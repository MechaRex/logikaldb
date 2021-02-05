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
import com.logikaldb.logikal.Result
import com.logikaldb.logikal.Variable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList

public fun Flow<Goal>.and(goals: List<Goal>): Flow<Goal> {
    return this.map { Constraint.and(listOf(it) + goals) }
}

public fun Flow<Goal>.and(vararg goals: Goal): Flow<Goal> {
    return this.and(goals.toList())
}

public fun Flow<Goal>.or(goals: List<Goal>): Flow<Goal> {
    return this.map { Constraint.or(listOf(it) + goals) }
}

public fun Flow<Goal>.or(vararg goals: Goal): Flow<Goal> {
    return this.or(goals.toList())
}

public suspend fun Flow<Goal>.selectBy(queryEngine: LogikalDB, selectedVariables: List<Variable>): List<Result> {
    return this.map { queryEngine.run(it) }
        .flattenMerge().filterNotNull()
        .map { it.valuesOf(selectedVariables) }.toList()
}

public suspend fun Flow<Goal>.selectBy(queryEngine: LogikalDB, vararg selectedVariables: Variable): List<Result> {
    return this.selectBy(queryEngine, selectedVariables.toList())
}

public fun Flow<Goal>.selectFlowBy(queryEngine: LogikalDB, selectedVariables: List<Variable>): Flow<Result> {
    return this.map { queryEngine.run(it) }
        .flattenMerge().filterNotNull()
        .map { it.valuesOf(selectedVariables) }
}

public fun Flow<Goal>.selectFlowBy(queryEngine: LogikalDB, vararg selectedVariables: Variable): Flow<Result> {
    return this.selectFlowBy(queryEngine, selectedVariables.toList())
}

private fun Flow<Goal>.join(joinGoal: Goal, otherGoalFlow: Flow<Goal>): Flow<Goal> {
    return this.map { thisGoal: Goal ->
        otherGoalFlow.map { otherGoal: Goal ->
            Constraint.and(thisGoal, otherGoal, joinGoal)
        }
    }.flattenMerge()
}

public fun Flow<Goal>.join(joinGoal: Goal, otherGoalFlows: List<Flow<Goal>>): Flow<Goal> {
    val joinedGoalFlow = this.map { Constraint.and(it, joinGoal) }
    return otherGoalFlows.fold(joinedGoalFlow) { accGoalFlow, currGoalFlow ->
        accGoalFlow.join(joinGoal, currGoalFlow)
    }
}

public fun Flow<Goal>.join(joinGoal: Goal, vararg otherGoalFlows: Flow<Goal>): Flow<Goal> {
    return this.join(joinGoal, otherGoalFlows.toList())
}
