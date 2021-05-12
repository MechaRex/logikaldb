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

public data class VariableTypedMap(private val variableValueMap: Map<VariableTyped<*>, Any> = mapOf()) {

    public tailrec fun <T> valueOf(variable: VariableTyped<T>): T? {
        val value = variableValueMap[variable]
        return if (value != null && value is VariableTyped<*>) {
            valueOf(value as VariableTyped<T>)
        } else {
            value as T?
        }
    }

    public fun <T> hasValue(variable: VariableTyped<T>): Boolean {
        val value = valueOf(variable)
        return value !is VariableTyped<*> && value != null
    }

    internal fun keys(): Set<VariableTyped<*>> {
        return variableValueMap.keys
    }

    internal fun plus(variableValuePair: Pair<VariableTyped<*>, Any>): VariableTypedMap {
        return VariableTypedMap(variableValueMap + variableValuePair)
    }
}
