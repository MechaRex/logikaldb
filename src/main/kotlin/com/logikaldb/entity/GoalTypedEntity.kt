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
import com.logikaldb.logikal.GoalTyped
import com.logikaldb.logikal.Value
import com.logikaldb.logikal.VariableName

internal data class GoalTypedEntity(val goal: GoalV2)

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
internal sealed class ValueTypeEntityV2

@JsonTypeName("value")
internal data class ValueEntityV2(val value: Value) : ValueTypeEntityV2()

@JsonTypeName("variable")
internal data class VariableEntityV2(val variableName: VariableName, val variableType: Class<*>) : ValueTypeEntityV2()

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
public sealed class GoalV2

@JsonTypeName("equal")
internal data class EqualEntityV2(val firstValueEntity: ValueTypeEntityV2, val secondValueEntity: ValueTypeEntityV2) : GoalV2()

@JsonTypeName("and")
internal data class AndEntityV2(val goals: List<GoalV2>) : GoalV2()

@JsonTypeName("or")
internal data class OrEntityV2(val goals: List<GoalV2>) : GoalV2()

@JsonTypeName("constraint")
internal data class ConstraintEntityV2(
    val constraintName: ConstraintName,
    val parameters: List<ValueTypeEntityV2>,
    @JsonIgnore val constraintGoal: GoalTyped?
) : GoalV2()
