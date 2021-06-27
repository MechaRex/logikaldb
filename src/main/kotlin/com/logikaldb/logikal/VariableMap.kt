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

public data class VariableMap(private val variableValueMap: Map<Variable<*>, Any> = mapOf()) {

    /**
     * Gives back the value of the variable in the state.
     *
     * @param variable provided variable
     * @return value of the provided value or null if the variable doesn't have a value
     * */
    public tailrec fun <T> valueOf(variable: Variable<T>): T? {
        val value = variableValueMap[variable]
        return if (value != null && value is Variable<*>) {
            valueOf(value as Variable<T>)
        } else {
            value as T?
        }
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
    public tailrec fun dynamicValueOf(value: Value): Value {
        return if (value is Variable<*>) {
            val variableValue = variableValueMap[value]
            if (variableValue != null) {
                dynamicValueOf(variableValue)
            } else {
                value
            }
        } else {
            value
        }
    }

    /**
     * Shows if a variable has a value in the state.
     *
     * @param variable variable to check
     * @return does the variable have a value
     * */
    public fun <T> hasValue(variable: Variable<T>): Boolean {
        val value = valueOf(variable)
        return value !is Variable<*> && value != null
    }

    internal fun keys(): Set<Variable<*>> {
        return variableValueMap.keys
    }

    internal fun plus(variableValuePair: Pair<Variable<*>, Any>): VariableMap {
        return VariableMap(variableValueMap + variableValuePair)
    }
}
