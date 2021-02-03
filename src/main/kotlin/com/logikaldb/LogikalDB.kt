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

import com.logikaldb.converter.GoalConverter
import com.logikaldb.database.DatabaseHandler
import com.logikaldb.entity.Goal
import com.logikaldb.entity.GoalEntity
import com.logikaldb.logikal.State
import com.logikaldb.serializer.EntitySerializer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

public class LogikalDB(constraintLibraries: List<ConstraintLibrary>, fdbVersion: Int = 620, clusterFilePath: String? = null) {
    private val entitySerializer: EntitySerializer = EntitySerializer()
    private val databaseHandler: DatabaseHandler = DatabaseHandler(fdbVersion, clusterFilePath)
    private val goalConverter: GoalConverter

    init {
        val mergedLibraries = StdLib.registerConstraintLibraries(constraintLibraries + StdLib)
        goalConverter = GoalConverter(mergedLibraries)
    }

    public constructor(vararg constraintLibraries: ConstraintLibrary) : this(constraintLibraries.toList())

    public fun read(directoryPath: List<String>, key: String): Flow<Goal> {
        val serializedValueFlow = databaseHandler.read(directoryPath, key)
        val goalEntityFlow = serializedValueFlow.filterNotNull().map(entitySerializer::deserialize)
        return goalEntityFlow.map { it.goal }
    }

    public suspend fun write(directoryPath: List<String>, key: String, value: Goal) {
        val goalEntity = GoalEntity(value)
        val serializedValue = entitySerializer.serialize(goalEntity)
        databaseHandler.write(directoryPath, key, serializedValue)
    }

    public fun run(goal: Goal, state: State = State()): Flow<State?> {
        val logikalGoal = goalConverter.convertToGoal(GoalEntity(goal))
        return logikalGoal(state)
    }
}
