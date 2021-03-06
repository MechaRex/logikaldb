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

import com.logikaldb.converter.ValueConverter
import com.logikaldb.entity.AndEntity
import com.logikaldb.entity.Constraint
import com.logikaldb.entity.ConstraintFunEntity
import com.logikaldb.entity.EqualEntity
import com.logikaldb.entity.OrEntity
import com.logikaldb.logikal.Field
import com.logikaldb.logikal.FieldConstraint
import com.logikaldb.logikal.Logikal
import com.logikaldb.logikal.Name
import com.logikaldb.logikal.Value

/**
 * Constraint object's responsibility is to handle every kind of constraint creation.
 * */
public object ConstraintFactory {

    /**
     * Creates a logical field.
     *
     * @param name name of the field
     * @return logical field
     * */
    public fun <T> field(name: Name, type: Class<T>): Field<T> {
        return Field(name, type)
    }

    /**
     * Creates a equality constraint between the firstValue and the secondValue.
     * [eq] is a constraint constructor.
     * Equality means that [field] == [value] and [value] == [field].
     *
     * @param field field in the constraint
     * @param value value in the constraint
     * @return equality constraint
     * */
    public fun <T> eq(field: Field<T>, value: T): Constraint {
        val firstValueEntity = ValueConverter.convertToValueTypeEntity(field)
        val secondValueEntity = ValueConverter.convertToValueTypeEntity(value as Value)
        return EqualEntity(firstValueEntity, secondValueEntity)
    }

    /**
     * Creates a equality constraint between the firstValue and the secondValue.
     * [eq] is a constraint constructor.
     * Equality means that [firstField] == [secondField] and [secondField] == [firstField].
     *
     * @param firstField first field in the constraint
     * @param secondField second field in the constraint
     * @return equality constraint
     * */
    public fun <T> eq(firstField: Field<T>, secondField: Field<T>): Constraint {
        val firstValueEntity = ValueConverter.convertToValueTypeEntity(firstField)
        val secondValueEntity = ValueConverter.convertToValueTypeEntity(secondField)
        return EqualEntity(firstValueEntity, secondValueEntity)
    }

    /**
     * Creates a custom constraint with it's own custom logic.
     * [create] is a constraint constructor.
     * Custom logic needs to follow the [FieldConstraint] functional interface, which is basically a state filter: (State) -> State?.
     *
     * @param parameterValues values used in the custom constraint
     * @param fieldConstraint implementation of the custom constraint
     * @return custom constraint
     * */
    public fun create(
        parameterValues: List<Value>,
        fieldConstraint: FieldConstraint
    ): Constraint {
        val constrainedFields = parameterValues.filterIsInstance<Field<*>>()
        val constraintFun = Logikal.constraint(constrainedFields, fieldConstraint)
        return ConstraintFunEntity(constraintFun)
    }

    /**
     * Creates an and constraint.
     * [and] is a constraint combinator.
     * And means that every constraint in it will need to be true at the same time.
     *
     * @param constraints list of constraints that will be combined
     * @return and constraint
     * */
    public fun and(constraints: List<Constraint>): Constraint {
        return AndEntity(constraints)
    }

    /**
     * Creates an and constraint.
     * [and] is a constraint combinator.
     * And means that every constraint in it will need to be true at the same time.
     *
     * @param constraints list of constraints that will be combined
     * @return and constraint
     * */
    public fun and(vararg constraints: Constraint): Constraint {
        return AndEntity(constraints.toList())
    }

    /**
     * Create an or constraint.
     * [or] is a constraint combinator.
     * Or means that every constraint separately can be true.
     *
     * @param constraints list of constraints that will be combined
     * @return or constraint
     * */
    public fun or(constraints: List<Constraint>): Constraint {
        return OrEntity(constraints)
    }

    /**
     * Create an or constraint.
     * [or] is a constraint combinator.
     * Or means that every constraint separately can be true.
     *
     * @param constraints list of constraints that will be combined
     * @return or constraint
     * */
    public fun or(vararg constraints: Constraint): Constraint {
        return OrEntity(constraints.toList())
    }
}
