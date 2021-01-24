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
import com.logikaldb.converter.ValueConverter
import com.logikaldb.entity.AndEntity
import com.logikaldb.entity.ConstraintEntity
import com.logikaldb.entity.EqualEntity
import com.logikaldb.entity.Goal
import com.logikaldb.entity.OrEntity
import com.logikaldb.logikal.Logikal
import com.logikaldb.logikal.Value
import com.logikaldb.logikal.Variable
import com.logikaldb.logikal.VariableConstraint
import com.logikaldb.logikal.VariableName

public object Constraint {

    public fun vr(variableName: VariableName): Variable {
        return Variable(variableName)
    }

    public fun eq(firstValue: Value, secondValue: Value): Goal {
        val firstValueEntity = ValueConverter.convertToValueEntity(firstValue)
        val secondValueEntity = ValueConverter.convertToValueEntity(secondValue)
        return EqualEntity(firstValueEntity, secondValueEntity)
    }

    public fun create(
        constraintReference: ConstraintFun,
        parameterValues: List<Value>,
        constraintFun: VariableConstraint
    ): Goal {
        val constraintName = ConstraintConverter.convertToConstraintName(constraintReference)
        val constrainedVariables = parameterValues.filterIsInstance<Variable>()
        val parameterEntities = parameterValues.map(ValueConverter::convertToValueEntity)
        val constraintGoal = Logikal.constraint(constrainedVariables, constraintFun)
        return ConstraintEntity(constraintName, parameterEntities, constraintGoal)
    }

    public fun create(
        constraintFun: ConstraintFun,
        vararg parameterValues: Value,
        variableConstraint: VariableConstraint
    ): Goal {
        return create(constraintFun, parameterValues.toList(), variableConstraint)
    }

    public fun and(goals: List<Goal>): Goal {
        return AndEntity(goals)
    }

    public fun and(vararg goals: Goal): Goal {
        return AndEntity(goals.toList())
    }

    public fun or(goals: List<Goal>): Goal {
        return OrEntity(goals)
    }

    public fun or(vararg goals: Goal): Goal {
        return OrEntity(goals.toList())
    }
}
