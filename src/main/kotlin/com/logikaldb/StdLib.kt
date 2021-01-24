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

public object StdLib : ConstraintLibrary {
    public fun notEq(firstValue: Value, secondValue: Value): Goal {
        return Constraint.create(::notEq, firstValue, secondValue) { state ->
            val valueOfFirst = state.valueOf(firstValue)
            val valueOfSecond = state.valueOf(secondValue)
            if (valueOfFirst == valueOfSecond) {
                null
            } else {
                state
            }
        }
    }

    public fun cmp(firstValue: Value, secondValue: Value, compareValue: Int): Goal {
        return Constraint.create(::cmp, firstValue, secondValue, compareValue) { state ->
            val valueOfFirst = state.valueOf(firstValue)
            val valueOfSecond = state.valueOf(secondValue)

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

    public fun inSet(variable: Variable, values: Set<Value>): Goal {
        return Constraint.or(values.map { Constraint.eq(variable, it) }.toList())
    }

    override fun exportConstraints(): ConstraintRegistry {
        return registerConstraints(::notEq, ::cmp)
    }
}
