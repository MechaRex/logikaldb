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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.logikaldb.serializer.ConstraintFunDeserializer
import com.logikaldb.serializer.ConstraintFunSerializer
import kotlinx.coroutines.flow.Flow

@JsonSerialize(using = ConstraintFunSerializer::class)
@JsonDeserialize(using = ConstraintFunDeserializer::class)
internal fun interface ConstraintFun : java.io.Serializable {
    operator fun invoke(state: State): Flow<State?>
}
