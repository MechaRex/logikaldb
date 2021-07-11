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
 * @param fieldValues field values
 * @param constraintMap field constraints
 * @constructor state instance
 * */
public data class State(
    private val fieldValues: FieldValues = FieldValues(),
    private val constraintMap: Map<Field<*>, List<FieldConstraint>> = emptyMap()
) {

    /**
     * Shows if a field has a value in the state.
     *
     * @param field field to check
     * @return does the field have a value
     * */
    public fun <T> hasValue(field: Field<T>): Boolean {
        return fieldValues.hasValue(field)
    }

    /**
     * Gives back the value of the provided value.
     * Value can be either [Value] or [Field].
     * If the value is [Field] then it gives back the value of the field in the state.
     * If the value is [Value] then it gives back the provided value itself.
     *
     * @param value provided value
     * @return value of the provided value
     * */
    public fun dynamicValueOf(value: Value): Value {
        return fieldValues.dynamicValueOf(value)
    }

    /**
     * Gives back the value of the field in the state.
     *
     * @param field provided field
     * @return value of the provided value or null if the field doesn't have a value
     * */
    public fun <T> valueOf(field: Field<T>): T? {
        return fieldValues.valueOf(field)
    }

    /**
     * Gives back the result of the state.
     * [FieldValues] is the values of the provided fields.
     *
     * @param fields provided fields that we are interested in
     * @return result of the state
     * */
    public fun valuesOf(fields: List<Field<*>>): FieldValues {
        return if (fields.isEmpty()) {
            val fieldValueMap = fieldValues.keys().associateWith { dynamicValueOf(it) }
            FieldValues(fieldValueMap)
        } else {
            val fieldValueMap = fields.associateWith { dynamicValueOf(it) }
            FieldValues(fieldValueMap)
        }
    }

    /**
     * Gives back the result of the state.
     * [FieldValues] is the values of the provided fields.
     *
     * @param fields provided fields that we are interested in
     * @return result of the state
     * */
    public fun valuesOf(vararg fields: Field<*>): FieldValues {
        return valuesOf(fields.toList())
    }

    internal fun hasConstraint(field: Field<*>): Boolean {
        return constraintMap.containsKey(field)
    }

    internal fun constraintsOf(field: Field<*>): List<FieldConstraint> {
        return constraintMap[field] ?: error("Field doesn't have constraints!")
    }

    internal fun addConstraint(field: Field<*>, fieldConstraint: FieldConstraint): State {
        return if (constraintMap.containsKey(field)) {
            val fieldConstraints = constraintMap.getValue(field)
            copy(constraintMap = constraintMap - field + Pair(field, fieldConstraints + fieldConstraint))
        } else {
            copy(constraintMap = constraintMap + Pair(field, listOf(fieldConstraint)))
        }
    }

    internal fun unify(firstValue: Value, secondValue: Value): State? {
        val evaluatedFirstValue = dynamicValueOf(firstValue)
        val evaluatedSecondValue = dynamicValueOf(secondValue)
        return when {
            evaluatedFirstValue == evaluatedSecondValue -> this
            evaluatedFirstValue is Field<*> -> addAndCheckField(evaluatedFirstValue, evaluatedSecondValue)
            evaluatedSecondValue is Field<*> -> addAndCheckField(evaluatedSecondValue, evaluatedFirstValue)
            else -> null
        }
    }

    private fun addAndCheckField(field: Field<*>, value: Value): State? {
        val newState = copy(fieldValues = fieldValues.plus(Pair(field, value)))
        val fieldConstraints = constraintMap.getOrDefault(field, emptyList())
        return fieldConstraints.filter { fieldConstraint ->
            val constrainedFields = getConstrainedFields(fieldConstraint)
            constrainedFields.all { newState.hasValue(it) }
        }.fold(newState, ::executeConstraint)
    }

    private fun getConstrainedFields(fieldConstraint: FieldConstraint): List<Field<*>> {
        return constraintMap.filterValues { it.contains(fieldConstraint) }.map { it.key }
    }

    private fun executeConstraint(state: State?, fieldConstraint: FieldConstraint): State? {
        return if (state != null) fieldConstraint(state) else null
    }
}
