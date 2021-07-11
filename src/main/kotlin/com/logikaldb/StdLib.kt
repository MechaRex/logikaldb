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
import com.logikaldb.logikal.Value

/**
 * Standard library of LogikalDB.
 * */
public object StdLib {

    /**
     * Creates a not equal field constraint.
     * [notEq] is a constraint constructor.
     * Not equal means that [field] != [expectedValue] and [expectedValue] != [field].
     *
     * @param field field to be checked
     * @param expectedValue expected value of the field
     * @return not equal constraint
     * */
    public fun <T> notEq(field: Field<T>, expectedValue: T): Constraint {
        return ConstraintFactory.create(listOf(field, expectedValue as Value)) { state ->
            val value = state.valueOf(field)
            if (value == expectedValue) {
                null
            } else {
                state
            }
        }
    }

    /**
     * Creates a not equal field constraint.
     * [notEq] is a constraint constructor.
     * Not equal means that [firstField] != [secondField] and [secondField] != [firstField].
     *
     * @param firstField first field to be checked
     * @param secondField second field to be checked
     * @return not equal constraint
     * */
    public fun <T> notEq(firstField: Field<T>, secondField: Field<T>): Constraint {
        return ConstraintFactory.create(listOf(firstField, secondField)) { state ->
            val firstValue = state.valueOf(firstField)
            val secondValue = state.valueOf(secondField)

            if (firstValue == secondValue) {
                null
            } else {
                state
            }
        }
    }

    /**
     * Creates a comparability constraint.
     * [cmp] is a constraint constructor.
     * Comparability means that [field] compareTo [value] == [expectedCompareValue].
     *
     * @param field field to be compared to
     * @param value value to be compared to
     * @param expectedCompareValue provided [compareTo] result value to match
     * @return comparability constraint
     * */
    public fun <T : Comparable<T>> cmp(field: Field<T>, value: T, expectedCompareValue: Int): Constraint {
        return ConstraintFactory.create(listOf(field, value, expectedCompareValue)) { state ->
            val valueOfField = state.valueOf(field)

            val compareResult = (valueOfField)?.compareTo(value)
            if (compareResult == expectedCompareValue) {
                state
            } else {
                null
            }
        }
    }

    /**
     * Creates a comparability constraint.
     * [cmp] is a constraint constructor.
     * Comparability means that [firstField] compareTo [secondField] == [expectedCompareValue].
     *
     * @param firstField first field to be compared to
     * @param secondField second field to be compared to
     * @param expectedCompareValue provided [compareTo] result value to match
     * @return comparability constraint
     * */
    public fun <T : Comparable<T>> cmp(firstField: Field<T>, secondField: Field<T>, expectedCompareValue: Int): Constraint {
        return ConstraintFactory.create(listOf(firstField, secondField, expectedCompareValue)) { state ->
            val firstValue = state.valueOf(firstField)
            val secondValue = state.valueOf(secondField)

            val compareResult = secondValue?.let { firstValue?.compareTo(it) }
            if (compareResult == expectedCompareValue) {
                state
            } else {
                null
            }
        }
    }

    /**
     * Creates an in set constraint.
     * [inSet] is a constraint constructor.
     * In set constrains means that the provided field can have the provided set of values.
     *
     * @param field provided field
     * @param values provided set of values
     * @return in set constraint
     * */
    public fun <T> inSet(field: Field<T>, values: Set<T>): Constraint {
        return ConstraintFactory.or(values.map { ConstraintFactory.eq(field, it) }.toList())
    }
}
