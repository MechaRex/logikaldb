/*Copyright 2021 Mecharex Kft.
This file is part of the logikaldb library.

The logikaldb library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

The logikaldb library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with the logikaldb library. If not, see <http://www.gnu.org/licenses/>.*/

package com.logikaldb

import com.logikaldb.converter.GoalTypedConverter
import com.logikaldb.database.DatabaseHandler
import com.logikaldb.entity.GoalTypedEntity
import com.logikaldb.entity.GoalV2
import com.logikaldb.logikal.StateTyped
import com.logikaldb.serializer.EntityTypedSerializer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

/**
 * LogikalDB that handles the database and the built in query/logical engine.
 *
 * @param constraintLibraries custom constraint libraries
 * @param fdbVersion version of FDB to use
 * @param clusterFilePath path to the FDB cluster file
 * @constructor creates a [LogikalDBTyped] instance
 * */
public class LogikalDBTyped(constraintLibraries: List<ConstraintLibraryTyped>, fdbVersion: Int = 620, clusterFilePath: String? = null) {
    private val entitySerializer: EntityTypedSerializer = EntityTypedSerializer()
    private val databaseHandler: DatabaseHandler = DatabaseHandler(fdbVersion, clusterFilePath)
    private val goalConverter: GoalTypedConverter

    init {
        val mergedLibraries = StdLibTyped.registerConstraintLibraries(constraintLibraries + StdLibTyped)
        goalConverter = GoalTypedConverter(mergedLibraries)
    }

    /**
     * LogikalDB that handles the database and the built in query/logical engine.
     *
     * @param constraintLibraries custom constraint libraries
     * @param fdbVersion version of FDB to use
     * @param clusterFilePath path to the FDB cluster file
     * */
    public constructor(vararg constraintLibraries: ConstraintLibraryTyped, fdbVersion: Int = 620, clusterFilePath: String? = null) :
        this(constraintLibraries.toList(), fdbVersion, clusterFilePath)

    /**
     * Reads a value from the database.
     *
     * @param directoryPath directory path to where the key is
     * @param key key of the value in the database
     * @return value flow from the database
     * */
    public fun read(directoryPath: List<String>, key: String): QueryTyped {
        val serializedValueFlow = databaseHandler.read(directoryPath, key)
        val goalEntityFlow = serializedValueFlow.filterNotNull().map(entitySerializer::deserialize)
        val goalFlow = goalEntityFlow.map { it.goal }
        return QueryTyped(this, goalFlow)
    }

    /**
     * Writes a value to the database.
     *
     * @param directoryPath directory path to where the key is
     * @param key key of the value in the database
     * @param value value to insert into the database
     * */
    public suspend fun write(directoryPath: List<String>, key: String, value: GoalV2) {
        val goalEntity = GoalTypedEntity(value)
        val serializedValue = entitySerializer.serialize(goalEntity)
        databaseHandler.write(directoryPath, key, serializedValue)
    }

    /**
     * Evaluates the provided constraint.
     *
     * @param goal provided constraint
     * @param state initial used for the evaluation
     * @return state flow result of the evaluation
     * */
    public fun run(goal: GoalV2, state: StateTyped = StateTyped()): Flow<StateTyped?> {
        val logikalGoal = goalConverter.convertToGoal(GoalTypedEntity(goal))
        return logikalGoal(state)
    }
}
