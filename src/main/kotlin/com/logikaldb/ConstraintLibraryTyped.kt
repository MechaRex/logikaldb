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

import com.logikaldb.converter.ConstraintConverterTyped

/**
 * Constraint library interface used for creating custom constraint libraries.
 * */
public interface ConstraintLibraryTyped {

    /**
     * Gives back a constraint registry that contains the provided custom constraint functions.
     * You should only use it in the overridden [exportConstraints] function.
     *
     * @param constraintFuns provided custom constraint functions
     * @return constraint registry
     * */
    public fun registerConstraints(constraintFuns: List<ConstraintFunTyped>): ConstraintRegistryTyped {
        return constraintFuns.map { ConstraintConverterTyped.convertToConstraintName(it) to it }.toMap()
    }

    /**
     * Gives back a constraint registry that contains the provided custom constraint functions.
     * You should only use it in the overridden [exportConstraints] function.
     *
     * @param constraintFuns provided custom constraint functions
     * @return constraint registry
     * */
    public fun registerConstraints(vararg constraintFuns: ConstraintFunTyped): ConstraintRegistryTyped {
        return registerConstraints(constraintFuns.toList())
    }

    /**
     * Gives back a constraint registry that contains the provided custom constraint libraries.
     * You should only use it in the overridden [exportConstraints] function.
     *
     * @param constraintLibraries provided custom constraint libraries
     * @return constraint registry
     * */
    public fun registerConstraintLibraries(constraintLibraries: List<ConstraintLibraryTyped>): ConstraintRegistryTyped {
        return constraintLibraries.map { it.exportConstraints() }.reduce(::mergeConstraintRegistry)
    }

    /**
     * Gives back a constraint registry that contains the provided custom constraint libraries.
     * You should only use it in the overridden [exportConstraints] function.
     *
     * @param constraintLibraries provided custom constraint libraries
     * @return constraint registry
     * */
    public fun registerConstraintLibraries(vararg constraintLibraries: ConstraintLibraryTyped): ConstraintRegistryTyped {
        return registerConstraintLibraries(constraintLibraries.toList())
    }

    /**
     * You need to implement this function by calling the different register constraint functions in it.
     * */
    public fun exportConstraints(): ConstraintRegistryTyped

    private fun mergeConstraintRegistry(first: ConstraintRegistryTyped, second: ConstraintRegistryTyped): ConstraintRegistryTyped {
        return first.plus(second)
    }
}
