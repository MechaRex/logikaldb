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
import com.logikaldb.Constraint.field
import com.logikaldb.StdLib.cmp
import com.logikaldb.StdLib.inSet
import com.logikaldb.StdLib.notEq
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.toList

class StdLibTest : StringSpec({

    "notEq(field, value) should filter out constrained field when constraint matches and field is defined after notEq" {
        val logikalDB = LogikalDB()
        val field = field("field", Integer::class.java)
        val goal = and(eq(field, Integer(42)), notEq(field, Integer(42)))

        val result = logikalDB.run(goal).filterNotNull().toList()

        result.shouldBeEmpty()
    }

    "notEq(field, value) should filter out constrained field when constraint matches and field is defined before notEq" {
        val logikalDB = LogikalDB()
        val field = field("field", Integer::class.java)
        val goal = and(notEq(field, Integer(42)), eq(field, Integer(42)))

        val result = logikalDB.run(goal).filterNotNull().toList()

        result.shouldBeEmpty()
    }

    "notEq(field, value) should filter out constrained field when constraint doesn't match and field is defined after notEq" {
        val logikalDB = LogikalDB()
        val field = field("field", Integer::class.java)
        val goal = and(eq(field, Integer(42)), notEq(field, Integer(64)))

        val result = logikalDB.run(goal).filterNotNull().toList()

        result.shouldNotBeEmpty()
    }

    "notEq(field, value) should filter out constrained field when constraint doesn't match and field is defined before notEq" {
        val logikalDB = LogikalDB()
        val field = field("field", Integer::class.java)
        val goal = and(notEq(field, Integer(42)), eq(field, Integer(64)))

        val result = logikalDB.run(goal).filterNotNull().toList()

        result.shouldNotBeEmpty()
    }

    "notEq(field, field) should filter out constrained fields when constraint matches and fields are defined after notEq" {
        val logikalDB = LogikalDB()
        val firstField = field("firstField", Integer::class.java)
        val secondField = field("secondField", Integer::class.java)
        val goal = and(
            eq(firstField, Integer(42)),
            eq(secondField, Integer(42)),
            notEq(firstField, secondField)
        )

        val result = logikalDB.run(goal).filterNotNull().toList()

        result.shouldBeEmpty()
    }

    "notEq(field, field) should filter out constrained fields when constraint matches and fields are defined before notEq" {
        val logikalDB = LogikalDB()
        val firstField = field("firstField", Integer::class.java)
        val secondField = field("secondField", Integer::class.java)
        val goal = and(
            notEq(firstField, secondField),
            eq(firstField, Integer(42)),
            eq(secondField, Integer(42))
        )

        val result = logikalDB.run(goal).filterNotNull().toList()

        result.shouldBeEmpty()
    }

    "notEq(field, field) should filter out constrained fields when constraint doesn't match and fields are defined after notEq" {
        val logikalDB = LogikalDB()
        val firstField = field("firstField", Integer::class.java)
        val secondField = field("secondField", Integer::class.java)
        val goal = and(
            eq(firstField, Integer(42)),
            eq(secondField, Integer(64)),
            notEq(firstField, secondField)
        )

        val result = logikalDB.run(goal).filterNotNull().toList()

        result.shouldNotBeEmpty()
    }

    "notEq(field, field) should filter out constrained fields when constraint doesn't match and fields are defined before notEq" {
        val logikalDB = LogikalDB()
        val firstField = field("firstField", Integer::class.java)
        val secondField = field("secondField", Integer::class.java)
        val goal = and(
            notEq(firstField, secondField),
            eq(firstField, Integer(42)),
            eq(secondField, Integer(64))
        )

        val result = logikalDB.run(goal).filterNotNull().toList()

        result.shouldNotBeEmpty()
    }

    "cmp(field, field) should give back constrained fields when constraint matches and fields are defined after cmp" {
        val logikalDB = LogikalDB()
        val firstField = field("firstField", Integer::class.java)
        val secondField = field("secondField", Integer::class.java)
        val goal = and(
            eq(firstField, Integer(42)),
            eq(secondField, Integer(64)),
            cmp(firstField, secondField, -1)
        )

        val result = logikalDB.run(goal).filterNotNull().toList()

        result.shouldNotBeEmpty()
    }

    "cmp(field, field) should give back constrained fields when constraint matches and fields are defined before cmp" {
        val logikalDB = LogikalDB()
        val firstField = field("firstField", Integer::class.java)
        val secondField = field("secondField", Integer::class.java)
        val goal = and(
            cmp(firstField, secondField, -1),
            eq(firstField, Integer(42)),
            eq(secondField, Integer(64))
        )

        val result = logikalDB.run(goal).filterNotNull().toList()

        result.shouldNotBeEmpty()
    }

    "cmp(field, field) should filter out constrained fields when constraint doesn't match and fields are defined after cmp" {
        val logikalDB = LogikalDB()
        val firstField = field("firstField", Integer::class.java)
        val secondField = field("secondField", Integer::class.java)
        val goal = and(
            eq(firstField, Integer(42)),
            eq(secondField, Integer(42)),
            cmp(firstField, secondField, 1)
        )

        val result = logikalDB.run(goal).filterNotNull().toList()

        result.shouldBeEmpty()
    }

    "cmp(field, field) should filter out constrained fields when constraint doesn't match and fields are defined before cmp" {
        val logikalDB = LogikalDB()
        val firstField = field("firstField", Integer::class.java)
        val secondField = field("secondField", Integer::class.java)
        val goal = and(
            cmp(firstField, secondField, 1),
            eq(firstField, Integer(42)),
            eq(secondField, Integer(42))
        )

        val result = logikalDB.run(goal).filterNotNull().toList()

        result.shouldBeEmpty()
    }

    "cmp(field, value) should give back constrained field when constraint matches and field is defined after cmp" {
        val logikalDB = LogikalDB()
        val field = field("field", Integer::class.java)
        val goal = and(
            eq(field, Integer(42)),
            cmp(field, Integer(64), -1)
        )

        val result = logikalDB.run(goal).filterNotNull().toList()

        result.shouldNotBeEmpty()
    }

    "cmp(field, value) should give back constrained field when constraint matches and field is defined before cmp" {
        val logikalDB = LogikalDB()
        val field = field("field", Integer::class.java)
        val goal = and(
            cmp(field, Integer(64), -1),
            eq(field, Integer(42))
        )

        val result = logikalDB.run(goal).filterNotNull().toList()

        result.shouldNotBeEmpty()
    }

    "cmp(field, value) should filter out constrained field when constraint doesn't match and field is defined after cmp" {
        val logikalDB = LogikalDB()
        val field = field("field", Integer::class.java)
        val goal = and(
            eq(field, Integer(42)),
            cmp(field, Integer(64), 1)
        )

        val result = logikalDB.run(goal).filterNotNull().toList()

        result.shouldBeEmpty()
    }

    "cmp(field, value) should filter out constrained field when constraint doesn't match and field is defined before cmp" {
        val logikalDB = LogikalDB()
        val field = field("field", Integer::class.java)
        val goal = and(
            cmp(field, Integer(64), 1),
            eq(field, Integer(42))
        )

        val result = logikalDB.run(goal).filterNotNull().toList()

        result.shouldBeEmpty()
    }

    "inSet(field, set of value) should give back constrained field when constraint matches and field is defined after inSet" {
        val logikalDB = LogikalDB()
        val field = field("field", Integer::class.java)
        val goal = and(
            eq(field, Integer(42)),
            inSet(field, setOf(Integer(21), Integer(42), Integer(64)))
        )

        val result = logikalDB.run(goal).filterNotNull().toList()

        result.shouldNotBeEmpty()
    }

    "inSet(field, set of value) should give back constrained field when constraint matches and field is defined before inSet" {
        val logikalDB = LogikalDB()
        val field = field("field", Integer::class.java)
        val goal = and(
            inSet(field, setOf(Integer(21), Integer(42), Integer(64))),
            eq(field, Integer(42))
        )

        val result = logikalDB.run(goal).filterNotNull().toList()

        result.shouldNotBeEmpty()
    }

    "inSet(field, set of value) should filter out constrained field when constraint doesn't match and field is defined after inSet" {
        val logikalDB = LogikalDB()
        val field = field("field", Integer::class.java)
        val goal = and(
            eq(field, Integer(42)),
            inSet(field, setOf(Integer(21), Integer(64)))
        )

        val result = logikalDB.run(goal).filterNotNull().toList()

        result.shouldBeEmpty()
    }

    "inSet(field, set of value) should filter out constrained field when constraint doesn't match and field is defined before inSet" {
        val logikalDB = LogikalDB()
        val field = field("field", Integer::class.java)
        val goal = and(
            inSet(field, setOf(Integer(21), Integer(64))),
            eq(field, Integer(42))
        )

        val result = logikalDB.run(goal).filterNotNull().toList()

        result.shouldBeEmpty()
    }
})
