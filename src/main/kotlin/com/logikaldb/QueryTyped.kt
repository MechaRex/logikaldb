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

import com.logikaldb.entity.GoalV2
import com.logikaldb.logikal.VariableTyped
import com.logikaldb.logikal.VariableTypedMap
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
 * @constructor creates a [QueryTyped] instance
 * */
public class QueryTyped(private val logikalDB: LogikalDBTyped, private var goalFlow: Flow<GoalV2>) {

    /**
     * And combines the database result and the provided constraints in an And constraint and gives back the modified [QueryTyped] instance.
     *
     * @param goals provided constraints
     * @return returns the modified [QueryTyped]
     * */
    public fun and(goals: List<GoalV2>): QueryTyped = apply {
        this.goalFlow = goalFlow.map { ConstraintTyped.and(listOf(it) + goals) }
    }

    /**
     * And combines the database result and the provided constraints in an And constraint and gives back the modified [QueryTyped] instance.
     *
     * @param goals provided constraints
     * @return returns the modified [QueryTyped]
     * */
    public fun and(vararg goals: GoalV2): QueryTyped = apply {
        this.and(goals.toList())
    }

    /**
     * Or combines the database result and the provided constraints in an Or constraint and gives back the modified [QueryTyped] instance.
     *
     * @param goals provided constraints
     * @return returns the modified [QueryTyped]
     * */
    public fun or(goals: List<GoalV2>): QueryTyped = apply {
        this.goalFlow = goalFlow.map { ConstraintTyped.or(listOf(it) + goals) }
    }

    /**
     * Or combines the database result and the provided constraints in an Or constraint and gives back the modified [QueryTyped] instance.
     *
     * @param goals provided constraints
     * @return returns the modified [QueryTyped]
     * */
    public fun or(vararg goals: GoalV2): QueryTyped = apply {
        this.or(goals.toList())
    }

    /**
     * Join combines together two queries based on a join goal and gives back the modified [QueryTyped] instance.
     *
     * @param joinGoal provided join constraint
     * @param otherQuery provided other query
     * @return returns the modified [QueryTyped]
     * */
    public fun join(joinGoal: GoalV2, otherQuery: QueryTyped): QueryTyped = apply {
        this.goalFlow = this.goalFlow.map { thisGoal: GoalV2 ->
            otherQuery.goalFlow.map { otherGoal: GoalV2 ->
                ConstraintTyped.and(thisGoal, otherGoal, joinGoal)
            }
        }.flattenMerge()
    }

    /**
     * Join combines together multiple queries based on a join goal and gives back the modified [QueryTyped] instance.
     *
     * @param joinGoal provided join constraint
     * @param otherQueries provided other queries
     * @return returns the modified [QueryTyped]
     * */
    public fun join(joinGoal: GoalV2, otherQueries: List<QueryTyped>): QueryTyped = apply {
        val otherGoalFlows = otherQueries.map { it.goalFlow }
        val allGoalFlows = otherGoalFlows.plus(this.goalFlow).plus(flowOf(joinGoal))
        this.goalFlow = allGoalFlows.reduce { accGoalFlow, currGoalFlow ->
            accGoalFlow.map { accGoal: GoalV2 ->
                currGoalFlow.map { currGoal: GoalV2 ->
                    ConstraintTyped.and(accGoal, currGoal)
                }
            }.flattenMerge()
        }
    }

    /**
     * Join combines together multiple queries based on a join goal and gives back the modified [QueryTyped] instance.
     *
     * @param joinGoal provided join constraint
     * @param otherQueries provided other queries
     * @return returns the modified [QueryTyped]
     * */
    public fun join(joinGoal: GoalV2, vararg otherQueries: QueryTyped): QueryTyped {
        return this.join(joinGoal, otherQueries.toList())
    }

    /**
     * Selects variables from the database results.
     * This is a terminal operation and can be taught as the build method of this query builder.
     *
     * @param selectedVariables provided variables that you are interested in
     * @return list of variable values that we are interested in
     * */
    public suspend fun select(selectedVariables: List<VariableTyped<*>>): List<VariableTypedMap> {
        return this.goalFlow.map { logikalDB.run(it) }
            .flattenMerge().filterNotNull()
            .map { it.valuesOf(selectedVariables) }.toList()
    }

    /**
     * Selects variables from the database results.
     * This is a terminal operation and can be taught as the build method of this query builder.
     *
     * @param selectedVariables provided variables that you are interested in
     * @return list of variable values that we are interested in
     * */
    public suspend fun select(vararg selectedVariables: VariableTyped<*>): List<VariableTypedMap> {
        return this.select(selectedVariables.toList())
    }

    /**
     * Selects variables from the database results.
     * Non-terminal version of [select] and can be taught as the build method of this query builder.
     *
     * @param selectedVariables provided variables that you are interested in
     * @return flow of variable values that we are interested in
     * */
    public fun selectFlow(selectedVariables: List<VariableTyped<*>>): Flow<VariableTypedMap> {
        return this.goalFlow.map { logikalDB.run(it) }
            .flattenMerge().filterNotNull()
            .map { it.valuesOf(selectedVariables) }
    }

    /**
     * Selects variables from the database results.
     * Non-terminal version of [select] and can be taught as the build method of this query builder.
     *
     * @param selectedVariables provided variables that you are interested in
     * @return flow of variable values that we are interested in
     * */
    public fun selectFlow(vararg selectedVariables: VariableTyped<*>): Flow<VariableTypedMap> {
        return selectFlow(selectedVariables.toList())
    }
}
