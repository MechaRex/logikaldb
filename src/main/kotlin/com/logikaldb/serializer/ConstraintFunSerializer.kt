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

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.logikaldb.logikal.ConstraintFun
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream

internal class ConstraintFunSerializer : JsonSerializer<ConstraintFun>() {
    override fun serialize(constraintFun: ConstraintFun?, jsonGenerator: JsonGenerator?, serializerProvider: SerializerProvider?) {
        ByteArrayOutputStream().use { byteArrayOutputStream ->
            ObjectOutputStream(byteArrayOutputStream).use { objectOutputStream ->
                objectOutputStream.writeObject(constraintFun)
                jsonGenerator!!.writeBinary(byteArrayOutputStream.toByteArray())
            }
        }
    }
}
