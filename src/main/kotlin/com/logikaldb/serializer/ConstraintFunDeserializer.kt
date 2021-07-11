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

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.logikaldb.logikal.ConstraintFun
import java.io.ByteArrayInputStream
import java.io.ObjectInputStream

internal class ConstraintFunDeserializer : JsonDeserializer<ConstraintFun>() {
    override fun deserialize(jsonParser: JsonParser?, ctxt: DeserializationContext?): ConstraintFun {
        val binaryValue = jsonParser!!.binaryValue
        ByteArrayInputStream(binaryValue).use { byteArrayInputStream ->
            ObjectInputStream(byteArrayInputStream).use { objectInputStream ->
                return objectInputStream.readObject() as ConstraintFun
            }
        }
    }
}
