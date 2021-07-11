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

public data class FieldValues(private val fieldValueMap: Map<Field<*>, Any> = mapOf()) {

    /**
     * Gives back the value of the field in the state.
     *
     * @param field provided field
     * @return value of the provided value or null if the field doesn't have a value
     * */
    public tailrec fun <T> valueOf(field: Field<T>): T? {
        val value = fieldValueMap[field]
        return if (value != null && value is Field<*>) {
            valueOf(value as Field<T>)
        } else {
            value as T?
        }
    }

    /**
     * Gives back the evaluated value of the provided value.
     * Value can be either [Value] or [Field].
     * If the value is [Field] then it gives back the value of the field in the state.
     * If the value is [Value] then it gives back the provided value itself.
     *
     * @param value provided value
     * @return value of the provided value
     * */
    public tailrec fun dynamicValueOf(value: Value): Value {
        return if (value is Field<*>) {
            val fieldValue = fieldValueMap[value]
            if (fieldValue != null) {
                dynamicValueOf(fieldValue)
            } else {
                value
            }
        } else {
            value
        }
    }

    /**
     * Shows if a field has a value in the state.
     *
     * @param field field to check
     * @return does the field have a value
     * */
    public fun <T> hasValue(field: Field<T>): Boolean {
        val value = valueOf(field)
        return value !is Field<*> && value != null
    }

    internal fun keys(): Set<Field<*>> {
        return fieldValueMap.keys
    }

    internal fun plus(fieldValuePair: Pair<Field<*>, Any>): FieldValues {
        return FieldValues(fieldValueMap + fieldValuePair)
    }
}
