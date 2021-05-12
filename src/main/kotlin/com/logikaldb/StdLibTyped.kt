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

import com.logikaldb.entity.GoalV2
import com.logikaldb.logikal.Value
import com.logikaldb.logikal.VariableTyped

/**
 * Standard library of LogikalDB.
 * */
public object StdLibTyped : ConstraintLibraryTyped {

    /**
     * Creates a not equal constraint.
     * [notEq] is a constraint constructor.
     * Not equal means that [firstValue] != [secondValue] and [secondValue] != [firstValue].
     *
     * @param firstValue first value in the constraint
     * @param secondValue second value in the constraint
     * @return not equal constraint
     * */
    public fun notEq(firstValue: Value, secondValue: Value): GoalV2 {
        return ConstraintTyped.create(::notEq, firstValue, secondValue) { state ->
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
     * Creates a comparability constraint.
     * [cmp] is a constraint constructor.
     * Comparability means that [firstValue] compareTo [secondValue] == [compareValue].
     *
     * @param firstValue first value in the constraint
     * @param secondValue second value in the constraint
     * @param compareValue provided [compareTo] result value to match
     * @return comparability constraint
     * */
    public fun cmp(firstValue: Value, secondValue: Value, compareValue: Int): GoalV2 {
        return ConstraintTyped.create(::cmp, firstValue, secondValue, compareValue) { state ->
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
     * Creates an in set constraint.
     * [inSet] is a constraint constructor.
     * In set constrains means that the provided variable has separately the provided set of values.
     *
     * @param variable provided variable
     * @param values provided set of values
     * @return in set constraint
     * */
    public fun inSet(variable: VariableTyped<*>, values: Set<Value>): GoalV2 {
        return ConstraintTyped.or(values.map { ConstraintTyped.eq(variable, it) }.toList())
    }

    /**
     * Internal function that you don't need to use.
     * */
    override fun exportConstraints(): ConstraintRegistryTyped {
        return registerConstraints(::notEq, ::cmp)
    }
}
