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

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.math.BigDecimal

class StateTest : StringSpec({
    "addConstraint adds a constraint to an empty constraint map" {
        val field = Field("field", String::class.java)
        val fieldConstraint = { state: State -> state }
        val underTest = State()

        val result = underTest.addConstraint(field, fieldConstraint)

        result.hasConstraint(field).shouldBeTrue()
        result.constraintsOf(field).shouldContainExactly(fieldConstraint)
    }

    "addConstraint adds a constraint to an non-empty constraint map" {
        val field = Field("field", String::class.java)
        val fieldConstraint = { state: State -> state }
        val underTest = State(constraintMap = mapOf(field to listOf(fieldConstraint)))

        val result = underTest.addConstraint(field, fieldConstraint)

        result.hasConstraint(field).shouldBeTrue()
        result.constraintsOf(field).shouldContainExactly(fieldConstraint, fieldConstraint)
    }

    "unify gives back the input state if the two primitive value is the same" {
        val underTest = State()

        val result = underTest.unify(1, 1)

        underTest.shouldBe(result)
    }

    "unify gives back the input state if the two field's value is the same" {
        val firstField = Field("firstField", Integer::class.java)
        val secondField = Field("secondField", Integer::class.java)
        val underTest = State(
            fieldValues = FieldValues(
                mapOf(
                    firstField to 1,
                    secondField to 1
                )
            )
        )

        val result = underTest.unify(firstField, secondField)

        underTest.shouldBe(result)
    }

    "unify gives back the input state if the two field's object value is the same" {
        val firstField = Field("firstField", BigDecimal::class.java)
        val secondField = Field("secondField", BigDecimal::class.java)
        val underTest = State(
            fieldValues = FieldValues(
                mapOf(
                    firstField to BigDecimal(1),
                    secondField to BigDecimal(1)
                )
            )
        )

        val result = underTest.unify(firstField, secondField)

        underTest.shouldBe(result)
    }

    "unify adds the new field to the value map" {
        val field = Field("field", Integer::class.java)
        val expectedResult = State(fieldValues = FieldValues(mapOf(field to 1)))
        val underTest = State()

        val result = underTest.unify(field, 1)

        result.shouldBe(expectedResult)
    }

    "unify adds the new field to the value map the other way" {
        val field = Field("field", Integer::class.java)
        val expectedResult = State(fieldValues = FieldValues(mapOf(field to 1)))
        val underTest = State()

        val result = underTest.unify(1, field)

        result.shouldBe(expectedResult)
    }

    "unify creates two equal empty fields and then assign the value to the second field through the first field" {
        val firstField = Field("firstField", Integer::class.java)
        val secondField = Field("secondField", Integer::class.java)
        val expectedResult = State(
            fieldValues = FieldValues(
                mapOf(
                    firstField to secondField,
                    secondField to 1
                )
            )
        )
        val underTest = State()

        val result = underTest.unify(firstField, secondField)?.unify(firstField, 1)

        result.shouldBe(expectedResult)
    }

    "unify creates two equal empty fields and then assign the value to the second field" {
        val firstField = Field("firstField", Integer::class.java)
        val secondField = Field("secondField", Integer::class.java)
        val expectedResult = State(
            fieldValues = FieldValues(
                mapOf(
                    firstField to secondField,
                    secondField to 1
                )
            )
        )
        val underTest = State()

        val result = underTest.unify(firstField, secondField)?.unify(secondField, 1)

        result.shouldBe(expectedResult)
    }

    "unify creates a first field with a value and then assign it to the second field" {
        val firstField = Field("firstField", Integer::class.java)
        val secondField = Field("secondField", Integer::class.java)
        val expectedResult = State(
            fieldValues = FieldValues(
                mapOf(
                    firstField to 1,
                    secondField to 1
                )
            )
        )
        val underTest = State()

        val result = underTest.unify(firstField, 1)?.unify(firstField, secondField)

        result.shouldBe(expectedResult)
    }

    "unify creates a first field with a value and then reverse assign it to the second field" {
        val firstField = Field("firstField", Integer::class.java)
        val secondField = Field("secondField", Integer::class.java)
        val expectedResult = State(
            fieldValues = FieldValues(
                mapOf(
                    firstField to 1,
                    secondField to 1
                )
            )
        )
        val underTest = State()

        val result = underTest.unify(firstField, 1)?.unify(secondField, firstField)

        result.shouldBe(expectedResult)
    }

    "unify adds the new field to the value map with matching id constraint" {
        val field = Field("field", Integer::class.java)
        val fieldConstraint = { state: State -> state }
        val expectedResult = State(fieldValues = FieldValues(mapOf(field to 1)), constraintMap = mapOf(field to listOf(fieldConstraint)))
        val underTest = State(constraintMap = mapOf(field to listOf(fieldConstraint)))

        val result = underTest.unify(field, 1)

        result.shouldBe(expectedResult)
    }

    "unify adds the new field to the value map with matching custom constraint" {
        val field = Field("field", Integer::class.java)
        val fieldConstraint = { _: State -> State(fieldValues = FieldValues(mapOf(field to 2))) }
        val expectedResult = State(fieldValues = FieldValues(mapOf(field to 2)))
        val underTest = State(constraintMap = mapOf(field to listOf(fieldConstraint)))

        val result = underTest.unify(field, 1)

        result.shouldBe(expectedResult)
    }

    "unify adds the new field to the value map with non-matching constraint" {
        val field = Field("field", Integer::class.java)
        val nonMatchingField = Field("nonMatchingField", Integer::class.java)
        val fieldConstraint = { _: State -> State(fieldValues = FieldValues(mapOf(field to 2))) }
        val expectedResult = State(fieldValues = FieldValues(mapOf(field to 1)), constraintMap = mapOf(nonMatchingField to listOf(fieldConstraint)))
        val underTest = State(constraintMap = mapOf(nonMatchingField to listOf(fieldConstraint)))

        val result = underTest.unify(field, 1)

        result.shouldBe(expectedResult)
    }

    "unify gives back null if the two primitive value is different" {
        val underTest = State()

        val result = underTest.unify(1, 2)

        result.shouldBeNull()
    }

    "unify gives back null if the two field's value is different" {
        val firstField = Field("firstField", Integer::class.java)
        val secondField = Field("secondField", Integer::class.java)
        val underTest = State(
            fieldValues = FieldValues(
                mapOf(
                    firstField to 1,
                    secondField to 2
                )
            )
        )

        val result = underTest.unify(firstField, secondField)

        result.shouldBeNull()
    }

    "unify gives back null if the two field's value and type is different" {
        val firstField = Field("firstField", String::class.java)
        val secondField = Field("secondField", BigDecimal::class.java)
        val underTest = State(
            fieldValues = FieldValues(
                mapOf(
                    firstField to "1",
                    secondField to BigDecimal(2)
                )
            )
        )

        val result = underTest.unify(firstField, secondField)

        result.shouldBeNull()
    }
})
