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

import com.logikaldb.entity.ValueEntityV2
import com.logikaldb.entity.ValueTypeEntityV2
import com.logikaldb.entity.VariableEntityV2
import com.logikaldb.logikal.Value
import com.logikaldb.logikal.VariableTyped

internal object ValueTypedConverter {
    fun convertToValueEntity(value: Value): ValueTypeEntityV2 {
        return when (value) {
            is VariableTyped<*> -> VariableEntityV2(value.variableName, value.variableType)
            else -> ValueEntityV2(value)
        }
    }

    fun convertToValue(valueTypeEntity: ValueTypeEntityV2): Value {
        return when (valueTypeEntity) {
            is VariableEntityV2 -> VariableTyped(valueTypeEntity.variableName, valueTypeEntity.variableType)
            is ValueEntityV2 -> valueTypeEntity.value
        }
    }
}
