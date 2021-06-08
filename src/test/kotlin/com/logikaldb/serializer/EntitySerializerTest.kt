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

package com.logikaldb.serializer

import com.logikaldb.entity.AndEntity
import com.logikaldb.entity.ConstraintEntity
import com.logikaldb.entity.EqualEntity
import com.logikaldb.entity.GoalEntity
import com.logikaldb.entity.OrEntity
import com.logikaldb.entity.ValueEntity
import com.logikaldb.entity.VariableEntity
import com.logikaldb.logikal.Logikal.equal
import io.kotest.core.spec.style.StringSpec
import io.kotest.property.Arb
import io.kotest.property.RandomSource
import io.kotest.property.Sample
import io.kotest.property.arbitrary.arb
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import io.kotest.property.forAll

class EntitySerializerTest : StringSpec({
    val underTest = EntitySerializer()

    fun createEqualEntity(randomPair: Pair<Sample<String>, Sample<Any>>): EqualEntity {
        return EqualEntity(VariableEntity(randomPair.first.value, String::class.java), ValueEntity(randomPair.second.value))
    }

    fun anyRandomValues(randomSource: RandomSource): Sequence<Sample<Any>> {
        val randomStrings = Arb.string().values(randomSource)
        val randomIntegers = Arb.int().values(randomSource)
        val randomDoubles = Arb.double().values(randomSource)
        return randomStrings + randomIntegers + randomDoubles
    }

    "A serialized and deserialized EqualEntity must be the same as the original entity" {
        val equalGoalEntityArb = arb { randomSource ->
            val randomVariables = Arb.string().values(randomSource)
            val randomValues = anyRandomValues(randomSource)
            randomVariables.zip(randomValues)
                .map(::createEqualEntity)
                .map { GoalEntity(it) }
        }
        forAll(equalGoalEntityArb) { equalGoalEntity ->
            underTest.deserialize(underTest.serialize(equalGoalEntity)) == equalGoalEntity
        }
    }

    "A serialized and deserialized ConstraintEntity must be the same when the constraintGoal is null" {
        val constraintGoalEntityArb = arb { randomSource ->
            val randomConstraints = Arb.string().values(randomSource)
            val randomParameters = Arb.list(Arb.string())
                .map { parameterNames -> parameterNames.map { VariableEntity(it, String::class.java) } }
                .values(randomSource)
            randomConstraints.zip(randomParameters)
                .map { ConstraintEntity(it.first.value, it.second.value, null) }
                .map { GoalEntity(it) }
        }
        forAll(constraintGoalEntityArb) { constraintGoalEntity ->
            underTest.deserialize(underTest.serialize(constraintGoalEntity)) == constraintGoalEntity
        }
    }

    "A serialized and deserialized ConstraintEntity must not be the same when the constraint goal is not null" {
        val constraintGoalEntityArb = arb { randomSource ->
            val randomConstraints = Arb.string().values(randomSource)
            val randomParameters = Arb.list(Arb.string())
                .map { parameterNames -> parameterNames.map { VariableEntity(it, String::class.java) } }
                .values(randomSource)
            randomConstraints.zip(randomParameters)
                .map { ConstraintEntity(it.first.value, it.second.value, equal(1, 1)) }
                .map { GoalEntity(it) }
        }
        forAll(constraintGoalEntityArb) { constraintGoalEntity ->
            val convertedConstraintGoalEntity = underTest.deserialize(underTest.serialize(constraintGoalEntity))
            convertedConstraintGoalEntity != constraintGoalEntity &&
                (constraintGoalEntity.goal as ConstraintEntity).constraintGoal != null &&
                (convertedConstraintGoalEntity.goal as ConstraintEntity).constraintGoal == null
        }
    }

    "A serialized and deserialized AndEntity must be the same as the original entity" {
        val equalEntityArb = arb { randomSource ->
            val randomVariables = Arb.string().values(randomSource)
            val randomValues = anyRandomValues(randomSource)
            randomVariables.zip(randomValues)
                .map(::createEqualEntity)
        }
        val andGoalEntityArb = arb { randomSource ->
            Arb.list(equalEntityArb, IntRange(0, 100)).values(randomSource)
                .map { AndEntity(it.value) }
                .map { GoalEntity(it) }
        }
        forAll(andGoalEntityArb) { andGoalEntity ->
            underTest.deserialize(underTest.serialize(andGoalEntity)) == andGoalEntity
        }
    }

    "A serialized and deserialized OrEntity must be the same as the original entity" {
        val equalEntityArb = arb { randomSource ->
            val randomVariables = Arb.string().values(randomSource)
            val randomValues = anyRandomValues(randomSource)
            randomVariables.zip(randomValues)
                .map(::createEqualEntity)
        }
        val orGoalEntityArb = arb { randomSource ->
            Arb.list(equalEntityArb, IntRange(0, 100)).values(randomSource)
                .map { OrEntity(it.value) }
                .map { GoalEntity(it) }
        }
        forAll(orGoalEntityArb) { orGoalEntity ->
            underTest.deserialize(underTest.serialize(orGoalEntity)) == orGoalEntity
        }
    }
})
