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

import com.logikaldb.converter.ConstraintConverter

public interface ConstraintLibrary {

    public fun registerConstraints(constraintFuns: List<ConstraintFun>): ConstraintRegistry {
        return constraintFuns.map { ConstraintConverter.convertToConstraintName(it) to it }.toMap()
    }

    public fun registerConstraints(vararg constraintFuns: ConstraintFun): ConstraintRegistry {
        return registerConstraints(constraintFuns.toList())
    }

    public fun registerConstraintLibraries(constraintLibraries: List<ConstraintLibrary>): ConstraintRegistry {
        return constraintLibraries.map { it.exportConstraints() }.reduce(::mergeConstraintRegistry)
    }

    public fun registerConstraintLibraries(vararg constraintLibraries: ConstraintLibrary): ConstraintRegistry {
        return registerConstraintLibraries(constraintLibraries.toList())
    }

    public fun exportConstraints(): ConstraintRegistry

    private fun mergeConstraintRegistry(first: ConstraintRegistry, second: ConstraintRegistry): ConstraintRegistry {
        return first.plus(second)
    }
}
