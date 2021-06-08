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
        val variable = Variable("variable", String::class.java)
        val variableConstraint = { state: State -> state }
        val underTest = State()

        val result = underTest.addConstraint(variable, variableConstraint)

        result.hasConstraint(variable).shouldBeTrue()
        result.constraintsOf(variable).shouldContainExactly(variableConstraint)
    }

    "addConstraint adds a constraint to an non-empty constraint map" {
        val variable = Variable("variable", String::class.java)
        val variableConstraint = { state: State -> state }
        val underTest = State(constraintMap = mapOf(variable to listOf(variableConstraint)))

        val result = underTest.addConstraint(variable, variableConstraint)

        result.hasConstraint(variable).shouldBeTrue()
        result.constraintsOf(variable).shouldContainExactly(variableConstraint, variableConstraint)
    }

    "unify gives back the input state if the two primitive value is the same" {
        val underTest = State()

        val result = underTest.unify(1, 1)

        underTest.shouldBe(result)
    }

    "unify gives back the input state if the two variable's value is the same" {
        val firstVariable = Variable("firstVariable", Integer::class.java)
        val secondVariable = Variable("secondVariable", Integer::class.java)
        val underTest = State(
            valueMap = VariableMap(
                mapOf(
                    firstVariable to 1,
                    secondVariable to 1
                )
            )
        )

        val result = underTest.unify(firstVariable, secondVariable)

        underTest.shouldBe(result)
    }

    "unify gives back the input state if the two variable's object value is the same" {
        val firstVariable = Variable("firstVariable", BigDecimal::class.java)
        val secondVariable = Variable("secondVariable", BigDecimal::class.java)
        val underTest = State(
            valueMap = VariableMap(
                mapOf(
                    firstVariable to BigDecimal(1),
                    secondVariable to BigDecimal(1)
                )
            )
        )

        val result = underTest.unify(firstVariable, secondVariable)

        underTest.shouldBe(result)
    }

    "unify adds the new variable to the value map" {
        val variable = Variable("variable", Integer::class.java)
        val expectedResult = State(valueMap = VariableMap(mapOf(variable to 1)))
        val underTest = State()

        val result = underTest.unify(variable, 1)

        result.shouldBe(expectedResult)
    }

    "unify adds the new variable to the value map the other way" {
        val variable = Variable("variable", Integer::class.java)
        val expectedResult = State(valueMap = VariableMap(mapOf(variable to 1)))
        val underTest = State()

        val result = underTest.unify(1, variable)

        result.shouldBe(expectedResult)
    }

    "unify creates two equal empty variables and then assign the value to the second variable through the first variable" {
        val firstVariable = Variable("firstVariable", Integer::class.java)
        val secondVariable = Variable("secondVariable", Integer::class.java)
        val expectedResult = State(
            valueMap = VariableMap(
                mapOf(
                    firstVariable to secondVariable,
                    secondVariable to 1
                )
            )
        )
        val underTest = State()

        val result = underTest.unify(firstVariable, secondVariable)?.unify(firstVariable, 1)

        result.shouldBe(expectedResult)
    }

    "unify creates two equal empty variables and then assign the value to the second variable" {
        val firstVariable = Variable("firstVariable", Integer::class.java)
        val secondVariable = Variable("secondVariable", Integer::class.java)
        val expectedResult = State(
            valueMap = VariableMap(
                mapOf(
                    firstVariable to secondVariable,
                    secondVariable to 1
                )
            )
        )
        val underTest = State()

        val result = underTest.unify(firstVariable, secondVariable)?.unify(secondVariable, 1)

        result.shouldBe(expectedResult)
    }

    "unify creates a first variable with a value and then assign it to the second variable" {
        val firstVariable = Variable("firstVariable", Integer::class.java)
        val secondVariable = Variable("secondVariable", Integer::class.java)
        val expectedResult = State(
            valueMap = VariableMap(
                mapOf(
                    firstVariable to 1,
                    secondVariable to 1
                )
            )
        )
        val underTest = State()

        val result = underTest.unify(firstVariable, 1)?.unify(firstVariable, secondVariable)

        result.shouldBe(expectedResult)
    }

    "unify creates a first variable with a value and then reverse assign it to the second variable" {
        val firstVariable = Variable("firstVariable", Integer::class.java)
        val secondVariable = Variable("secondVariable", Integer::class.java)
        val expectedResult = State(
            valueMap = VariableMap(
                mapOf(
                    firstVariable to 1,
                    secondVariable to 1
                )
            )
        )
        val underTest = State()

        val result = underTest.unify(firstVariable, 1)?.unify(secondVariable, firstVariable)

        result.shouldBe(expectedResult)
    }

    "unify adds the new variable to the value map with matching id constraint" {
        val variable = Variable("variable", Integer::class.java)
        val variableConstraint = { state: State -> state }
        val expectedResult = State(valueMap = VariableMap(mapOf(variable to 1)), constraintMap = mapOf(variable to listOf(variableConstraint)))
        val underTest = State(constraintMap = mapOf(variable to listOf(variableConstraint)))

        val result = underTest.unify(variable, 1)

        result.shouldBe(expectedResult)
    }

    "unify adds the new variable to the value map with matching custom constraint" {
        val variable = Variable("variable", Integer::class.java)
        val variableConstraint = { _: State -> State(valueMap = VariableMap(mapOf(variable to 2))) }
        val expectedResult = State(valueMap = VariableMap(mapOf(variable to 2)))
        val underTest = State(constraintMap = mapOf(variable to listOf(variableConstraint)))

        val result = underTest.unify(variable, 1)

        result.shouldBe(expectedResult)
    }

    "unify adds the new variable to the value map with non-matching constraint" {
        val variable = Variable("variable", Integer::class.java)
        val nonMatchingVariable = Variable("nonMatchingVariable", Integer::class.java)
        val variableConstraint = { _: State -> State(valueMap = VariableMap(mapOf(variable to 2))) }
        val expectedResult = State(valueMap = VariableMap(mapOf(variable to 1)), constraintMap = mapOf(nonMatchingVariable to listOf(variableConstraint)))
        val underTest = State(constraintMap = mapOf(nonMatchingVariable to listOf(variableConstraint)))

        val result = underTest.unify(variable, 1)

        result.shouldBe(expectedResult)
    }

    "unify gives back null if the two primitive value is different" {
        val underTest = State()

        val result = underTest.unify(1, 2)

        result.shouldBeNull()
    }

    "unify gives back null if the two variable's value is different" {
        val firstVariable = Variable("firstVariable", Integer::class.java)
        val secondVariable = Variable("secondVariable", Integer::class.java)
        val underTest = State(
            valueMap = VariableMap(
                mapOf(
                    firstVariable to 1,
                    secondVariable to 2
                )
            )
        )

        val result = underTest.unify(firstVariable, secondVariable)

        result.shouldBeNull()
    }

    "unify gives back null if the two variable's value and type is different" {
        val firstVariable = Variable("firstVariable", String::class.java)
        val secondVariable = Variable("secondVariable", BigDecimal::class.java)
        val underTest = State(
            valueMap = VariableMap(
                mapOf(
                    firstVariable to "1",
                    secondVariable to BigDecimal(2)
                )
            )
        )

        val result = underTest.unify(firstVariable, secondVariable)

        result.shouldBeNull()
    }
})
