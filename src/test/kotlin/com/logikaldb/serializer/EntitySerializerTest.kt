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
import com.logikaldb.entity.ConstraintFunEntity
import com.logikaldb.entity.EqualEntity
import com.logikaldb.entity.FieldEntity
import com.logikaldb.entity.OrEntity
import com.logikaldb.entity.ValueEntity
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
        return EqualEntity(FieldEntity(randomPair.first.value, String::class.java), ValueEntity(randomPair.second.value))
    }

    fun anyRandomValues(randomSource: RandomSource): Sequence<Sample<Any>> {
        val randomStrings = Arb.string().values(randomSource)
        val randomIntegers = Arb.int().values(randomSource)
        val randomDoubles = Arb.double().values(randomSource)
        return randomStrings + randomIntegers + randomDoubles
    }

    "A serialized and deserialized EqualEntity must be the same as the original entity" {
        val equalConstraintEntityArb = arb { randomSource ->
            val randomFields = Arb.string().values(randomSource)
            val randomValues = anyRandomValues(randomSource)
            randomFields.zip(randomValues)
                .map(::createEqualEntity)
                .map { ConstraintEntity(it) }
        }
        forAll(equalConstraintEntityArb) { equalConstraintEntity ->
            underTest.deserialize(underTest.serialize(equalConstraintEntity)) == equalConstraintEntity
        }
    }

    "A serialized and deserialized ConstraintEntity is not the same when the constraint fun is defined" {
        val constraintConstraintEntityArb = arb { randomSource ->
            val randomConstraints = Arb.string().values(randomSource)
            val randomParameters = Arb.list(Arb.string())
                .map { parameterNames -> parameterNames.map { FieldEntity(it, String::class.java) } }
                .values(randomSource)
            randomConstraints.zip(randomParameters)
                .map { ConstraintFunEntity(equal(1, 1)) }
                .map { ConstraintEntity(it) }
        }
        forAll(constraintConstraintEntityArb) { constraintConstraintEntity ->
            val convertedConstraintConstraintEntity = underTest.deserialize(underTest.serialize(constraintConstraintEntity))
            convertedConstraintConstraintEntity != constraintConstraintEntity
        }
    }

    "A serialized and deserialized AndEntity must be the same as the original entity" {
        val equalEntityArb = arb { randomSource ->
            val randomFields = Arb.string().values(randomSource)
            val randomValues = anyRandomValues(randomSource)
            randomFields.zip(randomValues)
                .map(::createEqualEntity)
        }
        val andConstraintEntityArb = arb { randomSource ->
            Arb.list(equalEntityArb, IntRange(0, 100)).values(randomSource)
                .map { AndEntity(it.value) }
                .map { ConstraintEntity(it) }
        }
        forAll(andConstraintEntityArb) { andConstraintEntity ->
            underTest.deserialize(underTest.serialize(andConstraintEntity)) == andConstraintEntity
        }
    }

    "A serialized and deserialized OrEntity must be the same as the original entity" {
        val equalEntityArb = arb { randomSource ->
            val randomFields = Arb.string().values(randomSource)
            val randomValues = anyRandomValues(randomSource)
            randomFields.zip(randomValues)
                .map(::createEqualEntity)
        }
        val orConstraintEntityArb = arb { randomSource ->
            Arb.list(equalEntityArb, IntRange(0, 100)).values(randomSource)
                .map { OrEntity(it.value) }
                .map { ConstraintEntity(it) }
        }
        forAll(orConstraintEntityArb) { orConstraintEntity ->
            underTest.deserialize(underTest.serialize(orConstraintEntity)) == orConstraintEntity
        }
    }
})
