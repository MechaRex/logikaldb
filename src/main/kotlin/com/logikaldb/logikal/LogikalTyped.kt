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

package com.logikaldb.logikal

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

internal object LogikalTyped {
    fun constraint(constrainedVariables: List<VariableTyped<*>>, variableConstraint: VariableConstraintTyped): GoalTyped = { state ->
        val isEveryConstrainedVariableInitialized = constrainedVariables.all { state.hasValue(it) }
        val stateWithConstraints = constrainedVariables.fold(state, createConstraintRegister(variableConstraint))
        if (isEveryConstrainedVariableInitialized && stateWithConstraints != null) {
            flowOf(variableConstraint(stateWithConstraints))
        } else {
            flowOf(stateWithConstraints)
        }
    }

    private fun createConstraintRegister(variableConstraint: VariableConstraintTyped) = { state: StateTyped?, variable: VariableTyped<*> ->
        state?.addConstraint(variable, variableConstraint)
    }

    fun equal(firstValue: Value, secondValue: Value): GoalTyped = { state ->
        val newState = state.unify(firstValue, secondValue)
        flowOf(newState)
    }

    @FlowPreview
    fun or(goals: List<GoalTyped>): GoalTyped = { state ->
        val combinedGoals = goals.asFlow().map { it(state) }
        combinedGoals.flattenMerge()
    }

    @FlowPreview
    fun or(vararg goals: GoalTyped): GoalTyped = or(goals.toList())

    @FlowPreview
    fun and(firstGoal: GoalTyped, secondGoal: GoalTyped): GoalTyped = { state ->
        val firstGoalStateFlow = firstGoal(state)
        val combinedGoal = firstGoalStateFlow.filterNotNull().map { secondGoal(it) }
        combinedGoal.flattenMerge()
    }

    @FlowPreview
    fun and(goals: List<GoalTyped>): GoalTyped = { state ->
        val combinedAndGoal = goals.reduce(::and)
        combinedAndGoal(state)
    }

    @FlowPreview
    fun and(vararg goals: GoalTyped): GoalTyped = and(goals.toList())
}
