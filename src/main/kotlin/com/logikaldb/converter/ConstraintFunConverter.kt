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

package com.logikaldb.converter

import com.logikaldb.entity.AndEntity
import com.logikaldb.entity.Constraint
import com.logikaldb.entity.ConstraintEntity
import com.logikaldb.entity.ConstraintFunEntity
import com.logikaldb.entity.EqualEntity
import com.logikaldb.entity.OrEntity
import com.logikaldb.logikal.ConstraintFun
import com.logikaldb.logikal.Logikal
import kotlinx.coroutines.FlowPreview

internal class ConstraintFunConverter() {
    enum class ConstraintCombinatorType { NONE, AND, OR }
    data class Frame(val currentConstraintCombinatorType: ConstraintCombinatorType, val constraints: MutableList<ConstraintFun>, val constraintEntities: MutableList<Constraint>)

    @FlowPreview
    fun convertToConstraintFun(constraintEntity: ConstraintEntity): ConstraintFun {
        val initialFrame = createInitialFrame(constraintEntity.constraint)
        val stack = ArrayDeque<Frame>()
        stack.addLast(initialFrame)
        val finalFrame = Frame(ConstraintCombinatorType.NONE, mutableListOf(), mutableListOf())
        while (stack.isNotEmpty()) {
            val currentFrame = stack.removeLast()

            if (currentFrame.constraintEntities.isEmpty()) {
                finalizeCurrentFrame(currentFrame, finalFrame, stack)
            } else {
                processNextConstraintEntity(currentFrame, stack)
            }
        }
        if (finalFrame.constraints.size == 1) {
            return finalFrame.constraints.last()
        } else {
            error("Couldn't convert the constraint entity into a constraint fun!")
        }
    }

    private fun createInitialFrame(constraintEntity: Constraint): Frame {
        return when (constraintEntity) {
            is EqualEntity -> {
                Frame(ConstraintCombinatorType.NONE, mutableListOf(createEqualConstraintFun(constraintEntity)), mutableListOf())
            }
            is ConstraintFunEntity -> {
                Frame(ConstraintCombinatorType.NONE, mutableListOf(createConstraintFunConstraintFun(constraintEntity)), mutableListOf())
            }
            is AndEntity -> Frame(
                ConstraintCombinatorType.AND, mutableListOf(), constraintEntity.constraints.toMutableList()
            )
            is OrEntity -> Frame(
                ConstraintCombinatorType.OR, mutableListOf(), constraintEntity.constraints.toMutableList()
            )
        }
    }

    @FlowPreview
    private fun finalizeCurrentFrame(currentFrame: Frame, finalFrame: Frame, stack: ArrayDeque<Frame>) {
        when (currentFrame.currentConstraintCombinatorType) {
            ConstraintCombinatorType.NONE -> finalFrame.constraints.add(currentFrame.constraints.last())
            ConstraintCombinatorType.AND -> {
                val constraintFun = Logikal.and(currentFrame.constraints)
                if (stack.isEmpty()) {
                    finalFrame.constraints.add(constraintFun)
                } else {
                    val previousFrame = stack.last()
                    previousFrame.constraints.add(constraintFun)
                }
            }
            ConstraintCombinatorType.OR -> {
                val constraintFun = Logikal.or(currentFrame.constraints)
                if (stack.isEmpty()) {
                    finalFrame.constraints.add(constraintFun)
                } else {
                    val previousFrame = stack.last()
                    previousFrame.constraints.add(constraintFun)
                }
            }
        }
    }

    private fun processNextConstraintEntity(currentFrame: Frame, stack: ArrayDeque<Frame>) {
        when (val constraintEntity = currentFrame.constraintEntities.removeLast()) {
            is EqualEntity -> {
                currentFrame.constraints.add(createEqualConstraintFun(constraintEntity))
                stack.addLast(currentFrame)
            }
            is ConstraintFunEntity -> {
                currentFrame.constraints.add(createConstraintFunConstraintFun(constraintEntity))
                stack.addLast(currentFrame)
            }
            is AndEntity -> {
                stack.addLast(currentFrame)
                val nextFrame = Frame(
                    ConstraintCombinatorType.AND, mutableListOf(), constraintEntity.constraints.toMutableList()
                )
                stack.addLast(nextFrame)
            }
            is OrEntity -> {
                stack.addLast(currentFrame)
                val nextFrame = Frame(
                    ConstraintCombinatorType.OR, mutableListOf(), constraintEntity.constraints.toMutableList()
                )
                stack.addLast(nextFrame)
            }
        }
    }

    private fun createEqualConstraintFun(equalEntity: EqualEntity): ConstraintFun {
        val firstValue = ValueConverter.convertToValue(equalEntity.firstValueEntity)
        val secondValue = ValueConverter.convertToValue(equalEntity.secondValueEntity)
        return Logikal.equal(firstValue, secondValue)
    }

    private fun createConstraintFunConstraintFun(constraintFunEntity: ConstraintFunEntity): ConstraintFun {
        return constraintFunEntity.constraintFun
    }
}
