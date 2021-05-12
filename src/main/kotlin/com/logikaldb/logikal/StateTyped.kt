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
public data class StateTyped(
    private val valueMap: VariableTypedMap = VariableTypedMap(),
    private val constraintMap: Map<VariableTyped<*>, List<VariableConstraintTyped>> = emptyMap()
) {

    /**
     * Shows if a variable has a value in the state.
     *
     * @param variable variable to check
     * @return does the variable have a value
     * */
    public fun <T> hasValue(variable: VariableTyped<T>): Boolean {
        return valueMap.hasValue(variable)
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
    public fun dynamicValueOf(value: Value): Value {
        return if (value is VariableTyped<*>) {
            valueMap.valueOf(value) ?: value
        } else {
            value
        }
    }

    /**
     * Gives back the value of the variable in the state.
     *
     * @param variable provided variable
     * @return value of the provided value or null if the variable doesn't have a value
     * */
    public fun <T> valueOf(variable: VariableTyped<T>): T? {
        return valueMap.valueOf(variable)
    }

    /**
     * Gives back the result of the state.
     * [Result] is the values of the provided variables.
     *
     * @param variables provided variables that we are interested in
     * @return result of the state
     * */
    public fun valuesOf(variables: List<VariableTyped<*>>): VariableTypedMap {
        return if (variables.isEmpty()) {
            val variableValueMap = valueMap.keys().associateWith { dynamicValueOf(it) }
            VariableTypedMap(variableValueMap)
        } else {
            val variableValueMap = variables.associateWith { dynamicValueOf(it) }
            VariableTypedMap(variableValueMap)
        }
    }

    /**
     * Gives back the result of the state.
     * [Result] is the values of the provided variables.
     *
     * @param variables provided variables that we are interested in
     * @return result of the state
     * */
    public fun valuesOf(vararg variables: VariableTyped<*>): VariableTypedMap {
        return valuesOf(variables.toList())
    }

    internal fun hasConstraint(variable: VariableTyped<*>): Boolean {
        return constraintMap.containsKey(variable)
    }

    internal fun constraintsOf(variable: VariableTyped<*>): List<VariableConstraintTyped> {
        return constraintMap[variable] ?: error("Variable doesn't have constraints!")
    }

    internal fun addConstraint(variable: VariableTyped<*>, variableConstraint: VariableConstraintTyped): StateTyped {
        return if (constraintMap.containsKey(variable)) {
            val variableConstraints = constraintMap.getValue(variable)
            copy(constraintMap = constraintMap - variable + Pair(variable, variableConstraints + variableConstraint))
        } else {
            copy(constraintMap = constraintMap + Pair(variable, listOf(variableConstraint)))
        }
    }

    internal fun unify(firstValue: Value, secondValue: Value): StateTyped? {
        val evaluatedFirstValue = dynamicValueOf(firstValue)
        val evaluatedSecondValue = dynamicValueOf(secondValue)
        return when {
            evaluatedFirstValue == evaluatedSecondValue -> this
            evaluatedFirstValue is VariableTyped<*> -> addAndCheckVariable(evaluatedFirstValue, evaluatedSecondValue)
            evaluatedSecondValue is VariableTyped<*> -> addAndCheckVariable(evaluatedSecondValue, evaluatedFirstValue)
            else -> null
        }
    }

    private fun addAndCheckVariable(variable: VariableTyped<*>, value: Value): StateTyped? {
        val newState = copy(valueMap = valueMap.plus(Pair(variable, value)))
        val variableConstraints = constraintMap.getOrDefault(variable, emptyList())
        return variableConstraints.filter { variableConstraint ->
            val constrainedVariables = getConstrainedVariables(variableConstraint)
            constrainedVariables.all { newState.hasValue(it) }
        }.fold(newState, ::executeConstraint)
    }

    private fun getConstrainedVariables(variableConstraint: VariableConstraintTyped): List<VariableTyped<*>> {
        return constraintMap.filterValues { it.contains(variableConstraint) }.map { it.key }
    }

    private fun executeConstraint(state: StateTyped?, variableConstraint: VariableConstraintTyped): StateTyped? {
        return if (state != null) variableConstraint(state) else null
    }
}
