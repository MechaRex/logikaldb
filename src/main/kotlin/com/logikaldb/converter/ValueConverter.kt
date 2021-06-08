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

package com.logikaldb.converter

import com.logikaldb.entity.ValueEntity
import com.logikaldb.entity.ValueTypeEntity
import com.logikaldb.entity.VariableEntity
import com.logikaldb.logikal.Value
import com.logikaldb.logikal.Variable

internal object ValueConverter {
    fun convertToValueEntity(value: Value): ValueTypeEntity {
        return when (value) {
            is Variable<*> -> VariableEntity(value.variableName, value.variableType)
            else -> ValueEntity(value)
        }
    }

    fun convertToValue(valueTypeEntity: ValueTypeEntity): Value {
        return when (valueTypeEntity) {
            is VariableEntity -> Variable(valueTypeEntity.variableName, valueTypeEntity.variableType)
            is ValueEntity -> valueTypeEntity.value
        }
    }
}
