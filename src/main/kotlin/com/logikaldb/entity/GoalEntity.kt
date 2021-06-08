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

package com.logikaldb.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import com.logikaldb.ConstraintName
import com.logikaldb.logikal.GoalFun
import com.logikaldb.logikal.Value
import com.logikaldb.logikal.VariableName

internal data class GoalEntity(val goal: Goal)

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
internal sealed class ValueTypeEntity

@JsonTypeName("value")
internal data class ValueEntity(val value: Value) : ValueTypeEntity()

@JsonTypeName("variable")
internal data class VariableEntity(val variableName: VariableName, val variableType: Class<*>) : ValueTypeEntity()

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
public sealed class Goal

@JsonTypeName("equal")
internal data class EqualEntity(val firstValueEntity: ValueTypeEntity, val secondValueEntity: ValueTypeEntity) : Goal()

@JsonTypeName("and")
internal data class AndEntity(val goals: List<Goal>) : Goal()

@JsonTypeName("or")
internal data class OrEntity(val goals: List<Goal>) : Goal()

@JsonTypeName("constraint")
internal data class ConstraintEntity(
    val constraintName: ConstraintName,
    val parameters: List<ValueTypeEntity>,
    @JsonIgnore val constraintGoal: GoalFun?
) : Goal()
