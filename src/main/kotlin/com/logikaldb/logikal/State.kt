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

// TODO: We should think about mutable state if this is incredibly slow
public data class State(
    private val valueMap: Map<Variable, Value> = emptyMap(),
    private val constraintMap: Map<Variable, List<VariableConstraint>> = emptyMap()
) {

    public fun hasValue(variable: Variable): Boolean {
        return valueOf(variable) !is Variable
    }

    public tailrec fun valueOf(value: Value): Value {
        val variableValue = valueMap[value]
        return if (variableValue != null) {
            valueOf(variableValue)
        } else {
            value
        }
    }

    internal fun hasConstraint(variable: Variable): Boolean {
        return constraintMap.containsKey(variable)
    }

    internal fun constraintsOf(variable: Variable): List<VariableConstraint> {
        return constraintMap[variable] ?: error("Variable doesn't have constraints!")
    }

    internal fun addConstraint(variable: Variable, variableConstraint: VariableConstraint): State {
        return if (constraintMap.containsKey(variable)) {
            val variableConstraints = constraintMap.getValue(variable)
            copy(constraintMap = constraintMap - variable + Pair(variable, variableConstraints + variableConstraint))
        } else {
            copy(constraintMap = constraintMap + Pair(variable, listOf(variableConstraint)))
        }
    }

    internal fun unify(firstValue: Value, secondValue: Value): State? {
        val evaluatedFirstValue = valueOf(firstValue)
        val evaluatedSecondValue = valueOf(secondValue)
        return when {
            evaluatedFirstValue == evaluatedSecondValue -> this
            evaluatedFirstValue is Variable -> addAndCheckVariable(evaluatedFirstValue, evaluatedSecondValue)
            evaluatedSecondValue is Variable -> addAndCheckVariable(evaluatedSecondValue, evaluatedFirstValue)
            else -> null
        }
    }

    private fun addAndCheckVariable(variable: Variable, value: Value): State? {
        val newState = copy(valueMap = valueMap + Pair(variable, value))
        val variableConstraints = constraintMap.getOrDefault(variable, emptyList())
        return variableConstraints.filter { variableConstraint ->
            val constrainedVariables = getConstrainedVariables(variableConstraint)
            constrainedVariables.all { newState.hasValue(it) }
        }.fold(newState, ::executeConstraint)
    }

    private fun getConstrainedVariables(variableConstraint: VariableConstraint): List<Variable> {
        return constraintMap.filterValues { it.contains(variableConstraint) }.map { it.key }
    }

    private fun executeConstraint(state: State?, variableConstraint: VariableConstraint): State? {
        return if (state != null) variableConstraint(state) else null
    }
}
