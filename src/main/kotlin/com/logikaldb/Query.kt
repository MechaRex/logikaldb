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

import com.logikaldb.entity.Goal
import com.logikaldb.logikal.Field
import com.logikaldb.logikal.FieldValues
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList

/**
 * Query is a builder which helps building up the query by adding constraints to it.
 *
 * @param logikalDB [LogikalDB] instance that will be used as a query engine
 * @param goalFlow internal database result that will be modified by the builder
 * @constructor creates a [Query] instance
 * */
public class Query(private val logikalDB: LogikalDB, private var goalFlow: Flow<Goal>) {

    /**
     * And combines the database result and the provided constraints in an And constraint and gives back the modified [Query] instance.
     *
     * @param goals provided constraints
     * @return returns the modified [Query]
     * */
    public fun and(goals: List<Goal>): Query = apply {
        this.goalFlow = goalFlow.map { Constraint.and(listOf(it) + goals) }
    }

    /**
     * And combines the database result and the provided constraints in an And constraint and gives back the modified [Query] instance.
     *
     * @param goals provided constraints
     * @return returns the modified [Query]
     * */
    public fun and(vararg goals: Goal): Query = apply {
        this.and(goals.toList())
    }

    /**
     * Or combines the database result and the provided constraints in an Or constraint and gives back the modified [Query] instance.
     *
     * @param goals provided constraints
     * @return returns the modified [Query]
     * */
    public fun or(goals: List<Goal>): Query = apply {
        this.goalFlow = goalFlow.map { Constraint.or(listOf(it) + goals) }
    }

    /**
     * Or combines the database result and the provided constraints in an Or constraint and gives back the modified [Query] instance.
     *
     * @param goals provided constraints
     * @return returns the modified [Query]
     * */
    public fun or(vararg goals: Goal): Query = apply {
        this.or(goals.toList())
    }

    /**
     * Join combines together two queries based on a join goal and gives back the modified [Query] instance.
     *
     * @param joinGoal provided join constraint
     * @param otherQuery provided other query
     * @return returns the modified [Query]
     * */
    public fun join(joinGoal: Goal, otherQuery: Query): Query = apply {
        this.goalFlow = this.goalFlow.map { thisGoal: Goal ->
            otherQuery.goalFlow.map { otherGoal: Goal ->
                Constraint.and(thisGoal, otherGoal, joinGoal)
            }
        }.flattenMerge()
    }

    /**
     * Join combines together multiple queries based on a join goal and gives back the modified [Query] instance.
     *
     * @param joinGoal provided join constraint
     * @param otherQueries provided other queries
     * @return returns the modified [Query]
     * */
    public fun join(joinGoal: Goal, otherQueries: List<Query>): Query = apply {
        val otherGoalFlows = otherQueries.map { it.goalFlow }
        val allGoalFlows = otherGoalFlows.plus(this.goalFlow).plus(flowOf(joinGoal))
        this.goalFlow = allGoalFlows.reduce { accGoalFlow, currGoalFlow ->
            accGoalFlow.map { accGoal: Goal ->
                currGoalFlow.map { currGoal: Goal ->
                    Constraint.and(accGoal, currGoal)
                }
            }.flattenMerge()
        }
    }

    /**
     * Join combines together multiple queries based on a join goal and gives back the modified [Query] instance.
     *
     * @param joinGoal provided join constraint
     * @param otherQueries provided other queries
     * @return returns the modified [Query]
     * */
    public fun join(joinGoal: Goal, vararg otherQueries: Query): Query {
        return this.join(joinGoal, otherQueries.toList())
    }

    /**
     * Selects fields from the database results.
     * This is a terminal operation and can be taught as the build method of this query builder.
     *
     * @param selectedFields provided fields that you are interested in
     * @return list of field values that we are interested in
     * */
    public suspend fun select(selectedFields: List<Field<*>>): List<FieldValues> {
        return this.goalFlow.map { logikalDB.run(it) }
            .flattenMerge().filterNotNull()
            .map { it.valuesOf(selectedFields) }.toList()
    }

    /**
     * Selects fields from the database results.
     * This is a terminal operation and can be taught as the build method of this query builder.
     *
     * @param selectedFields provided fields that you are interested in
     * @return list of field values that we are interested in
     * */
    public suspend fun select(vararg selectedFields: Field<*>): List<FieldValues> {
        return this.select(selectedFields.toList())
    }

    /**
     * Selects fields from the database results.
     * Non-terminal version of [select] and can be taught as the build method of this query builder.
     *
     * @param selectedFields provided fields that you are interested in
     * @return flow of field values that we are interested in
     * */
    public fun selectFlow(selectedFields: List<Field<*>>): Flow<FieldValues> {
        return this.goalFlow.map { logikalDB.run(it) }
            .flattenMerge().filterNotNull()
            .map { it.valuesOf(selectedFields) }
    }

    /**
     * Selects fields from the database results.
     * Non-terminal version of [select] and can be taught as the build method of this query builder.
     *
     * @param selectedFields provided fields that you are interested in
     * @return flow of field values that we are interested in
     * */
    public fun selectFlow(vararg selectedFields: Field<*>): Flow<FieldValues> {
        return selectFlow(selectedFields.toList())
    }
}
