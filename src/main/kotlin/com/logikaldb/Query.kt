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

import com.logikaldb.entity.Constraint
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
 * @param constraintFlow internal database result that will be modified by the builder
 * @constructor creates a [Query] instance
 * */
public class Query(private val logikalDB: LogikalDB, private var constraintFlow: Flow<Constraint>) {

    /**
     * And combines the database result and the provided constraints in an And constraint and gives back the modified [Query] instance.
     *
     * @param constraints provided constraints
     * @return returns the modified [Query]
     * */
    public fun and(constraints: List<Constraint>): Query = apply {
        this.constraintFlow = constraintFlow.map { ConstraintFactory.and(listOf(it) + constraints) }
    }

    /**
     * And combines the database result and the provided constraints in an And constraint and gives back the modified [Query] instance.
     *
     * @param constraints provided constraints
     * @return returns the modified [Query]
     * */
    public fun and(vararg constraints: Constraint): Query = apply {
        this.and(constraints.toList())
    }

    /**
     * Or combines the database result and the provided constraints in an Or constraint and gives back the modified [Query] instance.
     *
     * @param constraints provided constraints
     * @return returns the modified [Query]
     * */
    public fun or(constraints: List<Constraint>): Query = apply {
        this.constraintFlow = constraintFlow.map { ConstraintFactory.or(listOf(it) + constraints) }
    }

    /**
     * Or combines the database result and the provided constraints in an Or constraint and gives back the modified [Query] instance.
     *
     * @param constraints provided constraints
     * @return returns the modified [Query]
     * */
    public fun or(vararg constraints: Constraint): Query = apply {
        this.or(constraints.toList())
    }

    /**
     * Join combines together two queries based on a join constraint and gives back the modified [Query] instance.
     *
     * @param joinConstraint provided join constraint
     * @param otherQuery provided other query
     * @return returns the modified [Query]
     * */
    public fun join(joinConstraint: Constraint, otherQuery: Query): Query = apply {
        this.constraintFlow = this.constraintFlow.map { thisConstraint: Constraint ->
            otherQuery.constraintFlow.map { otherConstraint: Constraint ->
                ConstraintFactory.and(thisConstraint, otherConstraint, joinConstraint)
            }
        }.flattenMerge()
    }

    /**
     * Join combines together multiple queries based on a join constraint and gives back the modified [Query] instance.
     *
     * @param joinConstraint provided join constraint
     * @param otherQueries provided other queries
     * @return returns the modified [Query]
     * */
    public fun join(joinConstraint: Constraint, otherQueries: List<Query>): Query = apply {
        val otherConstraintFlows = otherQueries.map { it.constraintFlow }
        val allConstraintFlows = otherConstraintFlows.plus(this.constraintFlow).plus(flowOf(joinConstraint))
        this.constraintFlow = allConstraintFlows.reduce { accConstraintFlow, currConstraintFlow ->
            accConstraintFlow.map { accConstraint: Constraint ->
                currConstraintFlow.map { currConstraint: Constraint ->
                    ConstraintFactory.and(accConstraint, currConstraint)
                }
            }.flattenMerge()
        }
    }

    /**
     * Join combines together multiple queries based on a join constraint and gives back the modified [Query] instance.
     *
     * @param joinConstraint provided join constraint
     * @param otherQueries provided other queries
     * @return returns the modified [Query]
     * */
    public fun join(joinConstraint: Constraint, vararg otherQueries: Query): Query {
        return this.join(joinConstraint, otherQueries.toList())
    }

    /**
     * Selects fields from the database results.
     * This is a terminal operation and can be taught as the build method of this query builder.
     *
     * @param selectedFields provided fields that you are interested in
     * @return list of field values that we are interested in
     * */
    public suspend fun select(selectedFields: List<Field<*>>): List<FieldValues> {
        return this.constraintFlow.map { logikalDB.run(it) }
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
        return this.constraintFlow.map { logikalDB.run(it) }
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
