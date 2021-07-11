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

import com.logikaldb.Constraint.and
import com.logikaldb.Constraint.eq
import com.logikaldb.Constraint.vr
import com.logikaldb.StdLib.cmp
import com.logikaldb.StdLib.inSet
import com.logikaldb.StdLib.notEq
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.toList

class StdLibTest : StringSpec({

    "notEq(variable, value) should filter out constrained variable when constraint matches and variable is defined after notEq" {
        val logikalDB = LogikalDB()
        val variable = vr("variable", Integer::class.java)
        val goal = and(eq(variable, Integer(42)), notEq(variable, Integer(42)))

        val result = logikalDB.run(goal).filterNotNull().toList()

        result.shouldBeEmpty()
    }

    "notEq(variable, value) should filter out constrained variable when constraint matches and variable is defined before notEq" {
        val logikalDB = LogikalDB()
        val variable = vr("variable", Integer::class.java)
        val goal = and(notEq(variable, Integer(42)), eq(variable, Integer(42)))

        val result = logikalDB.run(goal).filterNotNull().toList()

        result.shouldBeEmpty()
    }

    "notEq(variable, value) should filter out constrained variable when constraint doesn't match and variable is defined after notEq" {
        val logikalDB = LogikalDB()
        val variable = vr("variable", Integer::class.java)
        val goal = and(eq(variable, Integer(42)), notEq(variable, Integer(64)))

        val result = logikalDB.run(goal).filterNotNull().toList()

        result.shouldNotBeEmpty()
    }

    "notEq(variable, value) should filter out constrained variable when constraint doesn't match and variable is defined before notEq" {
        val logikalDB = LogikalDB()
        val variable = vr("variable", Integer::class.java)
        val goal = and(notEq(variable, Integer(42)), eq(variable, Integer(64)))

        val result = logikalDB.run(goal).filterNotNull().toList()

        result.shouldNotBeEmpty()
    }

    "notEq(variable, variable) should filter out constrained variables when constraint matches and variables are defined after notEq" {
        val logikalDB = LogikalDB()
        val firstVariable = vr("firstVariable", Integer::class.java)
        val secondVariable = vr("secondVariable", Integer::class.java)
        val goal = and(
            eq(firstVariable, Integer(42)),
            eq(secondVariable, Integer(42)),
            notEq(firstVariable, secondVariable)
        )

        val result = logikalDB.run(goal).filterNotNull().toList()

        result.shouldBeEmpty()
    }

    "notEq(variable, variable) should filter out constrained variables when constraint matches and variables are defined before notEq" {
        val logikalDB = LogikalDB()
        val firstVariable = vr("firstVariable", Integer::class.java)
        val secondVariable = vr("secondVariable", Integer::class.java)
        val goal = and(
            notEq(firstVariable, secondVariable),
            eq(firstVariable, Integer(42)),
            eq(secondVariable, Integer(42))
        )

        val result = logikalDB.run(goal).filterNotNull().toList()

        result.shouldBeEmpty()
    }

    "notEq(variable, variable) should filter out constrained variables when constraint doesn't match and variables are defined after notEq" {
        val logikalDB = LogikalDB()
        val firstVariable = vr("firstVariable", Integer::class.java)
        val secondVariable = vr("secondVariable", Integer::class.java)
        val goal = and(
            eq(firstVariable, Integer(42)),
            eq(secondVariable, Integer(64)),
            notEq(firstVariable, secondVariable)
        )

        val result = logikalDB.run(goal).filterNotNull().toList()

        result.shouldNotBeEmpty()
    }

    "notEq(variable, variable) should filter out constrained variables when constraint doesn't match and variables are defined before notEq" {
        val logikalDB = LogikalDB()
        val firstVariable = vr("firstVariable", Integer::class.java)
        val secondVariable = vr("secondVariable", Integer::class.java)
        val goal = and(
            notEq(firstVariable, secondVariable),
            eq(firstVariable, Integer(42)),
            eq(secondVariable, Integer(64))
        )

        val result = logikalDB.run(goal).filterNotNull().toList()

        result.shouldNotBeEmpty()
    }

    "cmp(variable, variable) should give back constrained variables when constraint matches and variables are defined after cmp" {
        val logikalDB = LogikalDB()
        val firstVariable = vr("firstVariable", Integer::class.java)
        val secondVariable = vr("secondVariable", Integer::class.java)
        val goal = and(
            eq(firstVariable, Integer(42)),
            eq(secondVariable, Integer(64)),
            cmp(firstVariable, secondVariable, -1)
        )

        val result = logikalDB.run(goal).filterNotNull().toList()

        result.shouldNotBeEmpty()
    }

    "cmp(variable, variable) should give back constrained variables when constraint matches and variables are defined before cmp" {
        val logikalDB = LogikalDB()
        val firstVariable = vr("firstVariable", Integer::class.java)
        val secondVariable = vr("secondVariable", Integer::class.java)
        val goal = and(
            cmp(firstVariable, secondVariable, -1),
            eq(firstVariable, Integer(42)),
            eq(secondVariable, Integer(64))
        )

        val result = logikalDB.run(goal).filterNotNull().toList()

        result.shouldNotBeEmpty()
    }

    "cmp(variable, variable) should filter out constrained variables when constraint doesn't match and variables are defined after cmp" {
        val logikalDB = LogikalDB()
        val firstVariable = vr("firstVariable", Integer::class.java)
        val secondVariable = vr("secondVariable", Integer::class.java)
        val goal = and(
            eq(firstVariable, Integer(42)),
            eq(secondVariable, Integer(42)),
            cmp(firstVariable, secondVariable, 1)
        )

        val result = logikalDB.run(goal).filterNotNull().toList()

        result.shouldBeEmpty()
    }

    "cmp(variable, variable) should filter out constrained variables when constraint doesn't match and variables are defined before cmp" {
        val logikalDB = LogikalDB()
        val firstVariable = vr("firstVariable", Integer::class.java)
        val secondVariable = vr("secondVariable", Integer::class.java)
        val goal = and(
            cmp(firstVariable, secondVariable, 1),
            eq(firstVariable, Integer(42)),
            eq(secondVariable, Integer(42))
        )

        val result = logikalDB.run(goal).filterNotNull().toList()

        result.shouldBeEmpty()
    }

    "cmp(variable, value) should give back constrained variable when constraint matches and variable is defined after cmp" {
        val logikalDB = LogikalDB()
        val variable = vr("variable", Integer::class.java)
        val goal = and(
            eq(variable, Integer(42)),
            cmp(variable, Integer(64), -1)
        )

        val result = logikalDB.run(goal).filterNotNull().toList()

        result.shouldNotBeEmpty()
    }

    "cmp(variable, value) should give back constrained variable when constraint matches and variable is defined before cmp" {
        val logikalDB = LogikalDB()
        val variable = vr("variable", Integer::class.java)
        val goal = and(
            cmp(variable, Integer(64), -1),
            eq(variable, Integer(42))
        )

        val result = logikalDB.run(goal).filterNotNull().toList()

        result.shouldNotBeEmpty()
    }

    "cmp(variable, value) should filter out constrained variable when constraint doesn't match and variable is defined after cmp" {
        val logikalDB = LogikalDB()
        val variable = vr("variable", Integer::class.java)
        val goal = and(
            eq(variable, Integer(42)),
            cmp(variable, Integer(64), 1)
        )

        val result = logikalDB.run(goal).filterNotNull().toList()

        result.shouldBeEmpty()
    }

    "cmp(variable, value) should filter out constrained variable when constraint doesn't match and variable is defined before cmp" {
        val logikalDB = LogikalDB()
        val variable = vr("variable", Integer::class.java)
        val goal = and(
            cmp(variable, Integer(64), 1),
            eq(variable, Integer(42))
        )

        val result = logikalDB.run(goal).filterNotNull().toList()

        result.shouldBeEmpty()
    }

    "inSet(variable, set of value) should give back constrained variable when constraint matches and variable is defined after inSet" {
        val logikalDB = LogikalDB()
        val variable = vr("variable", Integer::class.java)
        val goal = and(
            eq(variable, Integer(42)),
            inSet(variable, setOf(Integer(21), Integer(42), Integer(64)))
        )

        val result = logikalDB.run(goal).filterNotNull().toList()

        result.shouldNotBeEmpty()
    }

    "inSet(variable, set of value) should give back constrained variable when constraint matches and variable is defined before inSet" {
        val logikalDB = LogikalDB()
        val variable = vr("variable", Integer::class.java)
        val goal = and(
            inSet(variable, setOf(Integer(21), Integer(42), Integer(64))),
            eq(variable, Integer(42))
        )

        val result = logikalDB.run(goal).filterNotNull().toList()

        result.shouldNotBeEmpty()
    }

    "inSet(variable, set of value) should filter out constrained variable when constraint doesn't match and variable is defined after inSet" {
        val logikalDB = LogikalDB()
        val variable = vr("variable", Integer::class.java)
        val goal = and(
            eq(variable, Integer(42)),
            inSet(variable, setOf(Integer(21), Integer(64)))
        )

        val result = logikalDB.run(goal).filterNotNull().toList()

        result.shouldBeEmpty()
    }

    "inSet(variable, set of value) should filter out constrained variable when constraint doesn't match and variable is defined before inSet" {
        val logikalDB = LogikalDB()
        val variable = vr("variable", Integer::class.java)
        val goal = and(
            inSet(variable, setOf(Integer(21), Integer(64))),
            eq(variable, Integer(42))
        )

        val result = logikalDB.run(goal).filterNotNull().toList()

        result.shouldBeEmpty()
    }
})
