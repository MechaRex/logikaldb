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
import com.logikaldb.converter.ConstraintConverterTyped
import com.logikaldb.converter.ValueTypedConverter
import com.logikaldb.entity.AndEntityV2
import com.logikaldb.entity.ConstraintEntityV2
import com.logikaldb.entity.EqualEntityV2
import com.logikaldb.entity.GoalV2
import com.logikaldb.entity.OrEntityV2
import com.logikaldb.logikal.LogikalTyped
import com.logikaldb.logikal.Value
import com.logikaldb.logikal.VariableConstraintTyped
import com.logikaldb.logikal.VariableName
import com.logikaldb.logikal.VariableTyped

/**
 * Constraint object's responsibility is to handle every kind of constraint creation.
 * */
public object ConstraintTyped {
    /**
     * Creates a logical variable.
     *
     * @param variableName name of the variable
     * @return logical variable
     * */
    public fun <T> vr(variableName: VariableName, variableType: Class<T>): VariableTyped<T> {
        return VariableTyped(variableName, variableType)
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
    public fun eq(firstValue: Value, secondValue: Value): GoalV2 {
        val firstValueEntity = ValueTypedConverter.convertToValueEntity(firstValue)
        val secondValueEntity = ValueTypedConverter.convertToValueEntity(secondValue)
        return EqualEntityV2(firstValueEntity, secondValueEntity)
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
        constraintReference: ConstraintFunTyped,
        parameterValues: List<Value>,
        constraintFun: VariableConstraintTyped
    ): GoalV2 {
        val constraintName = ConstraintConverterTyped.convertToConstraintName(constraintReference)
        val constrainedVariables = parameterValues.filterIsInstance<VariableTyped<*>>()
        val parameterEntities = parameterValues.map(ValueTypedConverter::convertToValueEntity)
        val constraintGoal = LogikalTyped.constraint(constrainedVariables, constraintFun)
        return ConstraintEntityV2(constraintName, parameterEntities, constraintGoal)
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
        constraintReference: ConstraintFunTyped,
        vararg parameterValues: Value,
        constraintFun: VariableConstraintTyped
    ): GoalV2 {
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
    public fun and(goals: List<GoalV2>): GoalV2 {
        return AndEntityV2(goals)
    }

    /**
     * Creates an and constraint.
     * [and] is a constraint combinator.
     * And means that every constraint in it will need to be true at the same time.
     *
     * @param goals list of constraints that will be combined
     * @return and constraint
     * */
    public fun and(vararg goals: GoalV2): GoalV2 {
        return AndEntityV2(goals.toList())
    }

    /**
     * Create an or constraint.
     * [or] is a constraint combinator.
     * Or means that every constraint separately can be true.
     *
     * @param goals list of constraints that will be combined
     * @return or constraint
     * */
    public fun or(goals: List<GoalV2>): GoalV2 {
        return OrEntityV2(goals)
    }

    /**
     * Create an or constraint.
     * [or] is a constraint combinator.
     * Or means that every constraint separately can be true.
     *
     * @param goals list of constraints that will be combined
     * @return or constraint
     * */
    public fun or(vararg goals: GoalV2): GoalV2 {
        return OrEntityV2(goals.toList())
    }
}
