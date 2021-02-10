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

/**
 * State is the stack frame used in the embedded logical programming language.
 * You should not use the [State] constructor directly.
 *
 * @param valueMap variable values
 * @param constraintMap variable constraints
 * @constructor state instance
 * */
public data class State(
    private val valueMap: Result = emptyMap(),
    private val constraintMap: Map<Variable, List<VariableConstraint>> = emptyMap()
) {

    /**
     * Shows if a variable has a value in the state.
     *
     * @param variable variable to check
     * @return does the variable have a value
     * */
    public fun hasValue(variable: Variable): Boolean {
        return valueOf(variable) !is Variable
    }

    /**
     * Gives back the value of the provided value.
     * Value can be either [Value] or [Variable].
     * If the value is [Variable] then it gives back the value of the variable in the state.
     * If the value is [Value] then it gives back the provided value itself.
     *
     * @param value provided value
     * @return value of the provided value
     * */
    public tailrec fun valueOf(value: Value): Value {
        val variableValue = valueMap[value]
        return if (variableValue != null) {
            valueOf(variableValue)
        } else {
            value
        }
    }

    /**
     * Gives back the result of the state.
     * [Result] is the values of the provided variables.
     *
     * @param variables provided variables that we are interested in
     * @return result of the state
     * */
    public fun valuesOf(variables: List<Variable>): Result {
        return if (variables.isEmpty()) {
            valueMap.keys.map { it to valueOf(it) }.toMap()
        } else {
            variables.map { it to valueOf(it) }.toMap()
        }
    }

    /**
     * Gives back the result of the state.
     * [Result] is the values of the provided variables.
     *
     * @param variables provided variables that we are interested in
     * @return result of the state
     * */
    public fun valuesOf(vararg variables: Variable): Result {
        return valuesOf(variables.toList())
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
