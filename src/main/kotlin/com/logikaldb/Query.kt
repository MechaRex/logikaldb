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
     * Selects variables from the database results.
     * This is a terminal operation and can be taught as the build method of this query builder.
     *
     * @param selectedVariables provided variables that you are interested in
     * @return list of variable values that we are interested in
     * */
    public suspend fun select(selectedVariables: List<Variable>): List<Result> {
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
    public suspend fun select(vararg selectedVariables: Variable): List<Result> {
        return this.select(selectedVariables.toList())
    }

    /**
     * Selects variables from the database results.
     * Non-terminal version of [select] and can be taught as the build method of this query builder.
     *
     * @param selectedVariables provided variables that you are interested in
     * @return flow of variable values that we are interested in
     * */
    public fun selectFlow(selectedVariables: List<Variable>): Flow<Result> {
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
    public fun selectFlow(vararg selectedVariables: Variable): Flow<Result> {
        return selectFlow(selectedVariables.toList())
    }
}
