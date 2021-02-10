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

import com.logikaldb.Constraint.and
import com.logikaldb.Constraint.or
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

/**
 * Constraint object's responsibility is to handle every kind of constraint creation.
 * */
public object Constraint {

    /**
     * Creates a logical variable.
     *
     * @param variableName name of the variable
     * @return logical variable
     * */
    public fun vr(variableName: VariableName): Variable {
        return Variable(variableName)
    }

    /**
     * Creates an equality constraint between the firstValue and the secondValue.
     * [eq] is a constraint constructor.
     * Equality means that [firstValue] == [secondValue] and [secondValue] == [firstValue].
     *
     * @param firstValue first value in the constraint
     * @param secondValue second value in the constraint
     * @return equality constraint
     * */
    public fun eq(firstValue: Value, secondValue: Value): Goal {
        val firstValueEntity = ValueConverter.convertToValueEntity(firstValue)
        val secondValueEntity = ValueConverter.convertToValueEntity(secondValue)
        return EqualEntity(firstValueEntity, secondValueEntity)
    }

    /**
     * Creates a custom constraint with it's own custom logic.
     * [create] is a constraint constructor.
     * Custom logic needs to follow the [VariableConstraint] functional interface, which is basically a state filter: (State) -> State?.
     *
     * @param constraintReference function reference of the created custom constraint
     * @param parameterValues values used in the custom constraint
     * @param constraintFun implementation of the custom constraint
     * @return custom constraint
     * */
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

    /**
     * Creates a custom constraint with it's own custom logic.
     * [create] is a constraint constructor.
     * Custom logic needs to follow the [VariableConstraint] functional interface, which is basically a state filter: (State) -> State?.
     *
     * @param constraintReference function reference of the created custom constraint
     * @param parameterValues values used in the custom constraint
     * @param constraintFun implementation of the custom constraint
     * @return custom constraint
     * */
    public fun create(
        constraintReference: ConstraintFun,
        vararg parameterValues: Value,
        constraintFun: VariableConstraint
    ): Goal {
        return create(constraintReference, parameterValues.toList(), constraintFun)
    }

    /**
     * Creates an and constraint.
     * [and] is a constraint combinator.
     * And means that every constraint in it will need to be true at the same time.
     *
     * @param goals list of constraints that will be combined
     * @return and constraint
     * */
    public fun and(goals: List<Goal>): Goal {
        return AndEntity(goals)
    }

    /**
     * Creates an and constraint.
     * [and] is a constraint combinator.
     * And means that every constraint in it will need to be true at the same time.
     *
     * @param goals list of constraints that will be combined
     * @return and constraint
     * */
    public fun and(vararg goals: Goal): Goal {
        return AndEntity(goals.toList())
    }

    /**
     * Create an or constraint.
     * [or] is a constraint combinator.
     * Or means that every constraint separately can be true.
     *
     * @param goals list of constraints that will be combined
     * @return or constraint
     * */
    public fun or(goals: List<Goal>): Goal {
        return OrEntity(goals)
    }

    /**
     * Create an or constraint.
     * [or] is a constraint combinator.
     * Or means that every constraint separately can be true.
     *
     * @param goals list of constraints that will be combined
     * @return or constraint
     * */
    public fun or(vararg goals: Goal): Goal {
        return OrEntity(goals.toList())
    }
}
