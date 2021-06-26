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
import com.logikaldb.logikal.Value
import com.logikaldb.logikal.Variable

/**
 * Standard library of LogikalDB.
 * */
public object StdLib {

    /**
     * Creates a dynamic not equal constraint.
     * [notEq] is a constraint constructor.
     * Not equal means that [firstValue] != [secondValue] and [secondValue] != [firstValue].
     *
     * @param firstValue first value in the constraint
     * @param secondValue second value in the constraint
     * @return not equal constraint
     * */
    public fun notEqDynamic(firstValue: Value, secondValue: Value): Goal {
        return Constraint.create(firstValue, secondValue) { state ->
            val valueOfFirst = state.dynamicValueOf(firstValue)
            val valueOfSecond = state.dynamicValueOf(secondValue)
            if (valueOfFirst == valueOfSecond) {
                null
            } else {
                state
            }
        }
    }

    /**
     * Creates a typed not equal variable constraint.
     * [notEq] is a constraint constructor.
     * Not equal means that [variable] != [expectedValue] and [expectedValue] != [variable].
     *
     * @param variable variable to be checked
     * @param expectedValue expected value of the variable
     * @return not equal constraint
     * */
    public fun <T> notEq(variable: Variable<T>, expectedValue: T): Goal {
        return Constraint.create(listOf(variable, expectedValue)) { state ->
            val value = state.valueOf(variable)
            if (value == expectedValue) {
                null
            } else {
                state
            }
        }
    }

    /**
     * Creates a typed not equal variable constraint.
     * [notEq] is a constraint constructor.
     * Not equal means that [firstVariable] != [secondVariable] and [secondVariable] != [firstVariable].
     *
     * @param firstVariable first variable to be checked
     * @param secondVariable second variable to be checked
     * @return not equal constraint
     * */
    public fun <T> notEq(firstVariable: Variable<T>, secondVariable: Variable<T>): Goal {
        return Constraint.create(listOf(firstVariable, secondVariable)) { state ->
            val firstValue = state.valueOf(firstVariable)
            val secondValue = state.valueOf(secondVariable)

            if (firstValue == secondValue) {
                null
            } else {
                state
            }
        }
    }

    /**
     * Creates a dynamic comparability constraint.
     * [cmp] is a constraint constructor.
     * Comparability means that [firstValue] compareTo [secondValue] == [compareValue].
     *
     * @param firstValue first value in the constraint
     * @param secondValue second value in the constraint
     * @param compareValue provided [compareTo] result value to match
     * @return comparability constraint
     * */
    public fun cmpDynamic(firstValue: Value, secondValue: Value, compareValue: Int): Goal {
        return Constraint.create(firstValue, secondValue, compareValue) { state ->
            val valueOfFirst = state.dynamicValueOf(firstValue)
            val valueOfSecond = state.dynamicValueOf(secondValue)

            if (valueOfFirst::class != valueOfSecond::class) {
                throw IllegalArgumentException("Values must be of the same type!")
            }
            if (valueOfFirst !is Comparable<*> || valueOfSecond !is Comparable<*>) {
                throw IllegalArgumentException("Values must be comparable!")
            }

            val compareResult = (valueOfFirst as Comparable<Value>).compareTo(valueOfSecond)
            if (compareResult == compareValue) {
                state
            } else {
                null
            }
        }
    }

    /**
     * Creates a typed comparability constraint.
     * [cmp] is a constraint constructor.
     * Comparability means that [variable] compareTo [value] == [expectedCompareValue].
     *
     * @param variable variable to be compared to
     * @param value value to be compared to
     * @param expectedCompareValue provided [compareTo] result value to match
     * @return comparability constraint
     * */
    public fun <T : Comparable<T>> cmp(variable: Variable<T>, value: T, expectedCompareValue: Int): Goal {
        return Constraint.create(listOf(variable, value, expectedCompareValue)) { state ->
            val valueOfVariable = state.valueOf(variable)

            val compareResult = (valueOfVariable)?.compareTo(value)
            if (compareResult == expectedCompareValue) {
                state
            } else {
                null
            }
        }
    }

    /**
     * Creates a typed comparability constraint.
     * [cmp] is a constraint constructor.
     * Comparability means that [firstVariable] compareTo [secondVariable] == [expectedCompareValue].
     *
     * @param firstVariable first variable to be compared to
     * @param secondVariable second variable to be compared to
     * @param expectedCompareValue provided [compareTo] result value to match
     * @return comparability constraint
     * */
    public fun <T : Comparable<T>> cmp(firstVariable: Variable<T>, secondVariable: Variable<T>, expectedCompareValue: Int): Goal {
        return Constraint.create(listOf(firstVariable, secondVariable, expectedCompareValue)) { state ->
            val firstValue = state.valueOf(firstVariable)
            val secondValue = state.valueOf(secondVariable)

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
     * In set constrains means that the provided variable has separately the provided set of values.
     *
     * @param variable provided variable
     * @param values provided set of values
     * @return in set constraint
     * */
    public fun <T> inSet(variable: Variable<T>, values: Set<T>): Goal {
        return Constraint.or(values.map { Constraint.eq(variable, it) }.toList())
    }
}
