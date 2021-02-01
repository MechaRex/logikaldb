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

package com.logikaldb

import com.logikaldb.entity.Goal
import com.logikaldb.logikal.Variable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList

public fun Flow<Goal>.and(goals: List<Goal>): Flow<Goal> {
    return this.map { Constraint.and(listOf(it) + goals) }
}

public fun Flow<Goal>.and(vararg goals: Goal): Flow<Goal> {
    return this.and(goals.toList())
}

public fun Flow<Goal>.or(goals: List<Goal>): Flow<Goal> {
    return this.map { Constraint.or(listOf(it) + goals) }
}

public fun Flow<Goal>.or(vararg goals: Goal): Flow<Goal> {
    return this.or(goals.toList())
}

public suspend fun Flow<Goal>.select(logikalDB: LogikalDB, variables: List<Variable>): List<Row> {
    return this.map { logikalDB.run(it) }
        .flattenMerge().filterNotNull()
        .map { it.valueOf(variables) }.toList()
}

public suspend fun Flow<Goal>.select(logikalDB: LogikalDB, vararg variables: Variable): List<Row> {
    return this.select(logikalDB, variables.toList())
}

public fun Flow<Goal>.selectFlow(logikalDB: LogikalDB, variables: List<Variable>): Flow<Row> {
    return this.map { logikalDB.run(it) }
        .flattenMerge().filterNotNull()
        .map { it.valueOf(variables) }
}

public fun Flow<Goal>.selectFlow(logikalDB: LogikalDB, vararg variables: Variable): Flow<Row> {
    return this.selectFlow(logikalDB, variables.toList())
}
