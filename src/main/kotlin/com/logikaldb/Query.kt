package com.logikaldb

import com.logikaldb.entity.Goal
import com.logikaldb.logikal.Result
import com.logikaldb.logikal.Variable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList

public class Query(private val logikalDB: LogikalDB, private var goalFlow: Flow<Goal>) {
    public fun and(goals: List<Goal>): Query = apply {
        this.goalFlow = goalFlow.map { Constraint.and(listOf(it) + goals) }
    }

    public fun and(vararg goals: Goal): Query = apply {
        this.and(goals.toList())
    }

    public fun or(goals: List<Goal>): Query = apply {
        this.goalFlow = goalFlow.map { Constraint.or(listOf(it) + goals) }
    }

    public fun or(vararg goals: Goal): Query = apply {
        this.or(goals.toList())
    }

    public fun join(joinGoal: Goal, otherQuery: Query): Query = apply {
        this.goalFlow = this.goalFlow.map { thisGoal: Goal ->
            otherQuery.goalFlow.map { otherGoal: Goal ->
                Constraint.and(thisGoal, otherGoal, joinGoal)
            }
        }.flattenMerge()
    }

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

    public fun join(joinGoal: Goal, vararg otherQueries: Query): Query {
        return this.join(joinGoal, otherQueries.toList())
    }

    public suspend fun select(selectedVariables: List<Variable>): List<Result> {
        return this.goalFlow.map { logikalDB.run(it) }
            .flattenMerge().filterNotNull()
            .map { it.valuesOf(selectedVariables) }.toList()
    }

    public suspend fun select(vararg selectedVariables: Variable): List<Result> {
        return this.select(selectedVariables.toList())
    }

    public fun selectFlow(selectedVariables: List<Variable>): Flow<Result> {
        return this.goalFlow.map { logikalDB.run(it) }
            .flattenMerge().filterNotNull()
            .map { it.valuesOf(selectedVariables) }
    }

    public fun selectFlow(vararg selectedVariables: Variable): Flow<Result> {
        return selectFlow(selectedVariables.toList())
    }
}
