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

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import com.logikaldb.logikal.ConstraintFun
import com.logikaldb.logikal.Name
import com.logikaldb.logikal.Value

internal data class ConstraintEntity(val constraint: Constraint)

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
internal sealed class ValueTypeEntity

@JsonTypeName("value")
internal data class ValueEntity(val value: Value) : ValueTypeEntity()

@JsonTypeName("field")
internal data class FieldEntity(val name: Name, val type: Class<*>) : ValueTypeEntity()

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
public sealed class Constraint

@JsonTypeName("equal")
internal data class EqualEntity(val firstValueEntity: ValueTypeEntity, val secondValueEntity: ValueTypeEntity) : Constraint()

@JsonTypeName("and")
internal data class AndEntity(val constraints: List<Constraint>) : Constraint()

@JsonTypeName("or")
internal data class OrEntity(val constraints: List<Constraint>) : Constraint()

@JsonTypeName("constraintFun")
internal data class ConstraintFunEntity(val constraintFun: ConstraintFun) : Constraint()
