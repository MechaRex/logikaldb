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

import com.logikaldb.ConstraintRegistry
import com.logikaldb.entity.AndEntity
import com.logikaldb.entity.ConstraintEntity
import com.logikaldb.entity.EqualEntity
import com.logikaldb.entity.Goal
import com.logikaldb.entity.GoalEntity
import com.logikaldb.entity.OrEntity
import com.logikaldb.logikal.GoalFun
import com.logikaldb.logikal.Logikal
import kotlinx.coroutines.FlowPreview

internal class GoalConverter(private val constraintRegistry: ConstraintRegistry) {
    enum class GoalCombinatorType { NONE, AND, OR }
    data class Frame(val currentGoalEntity: GoalCombinatorType, val goals: MutableList<GoalFun>, val goalEntities: MutableList<Goal>)

    @FlowPreview
    fun convertToGoal(goalEntity: GoalEntity): GoalFun {
        val initialFrame = createInitialFrame(goalEntity.goal)
        val stack = ArrayDeque<Frame>()
        stack.addLast(initialFrame)
        val finalFrame = Frame(GoalCombinatorType.NONE, mutableListOf(), mutableListOf())
        while (stack.isNotEmpty()) {
            val currentFrame = stack.removeLast()

            if (currentFrame.goalEntities.isEmpty()) {
                finalizeCurrentFrame(currentFrame, finalFrame, stack)
            } else {
                processNextGoalEntity(currentFrame, stack)
            }
        }
        if (finalFrame.goals.size == 1) {
            return finalFrame.goals.last()
        } else {
            error("Couldn't convert the goal entity into a goal!")
        }
    }

    private fun createInitialFrame(goalEntity: Goal): Frame {
        return when (goalEntity) {
            is EqualEntity -> {
                Frame(GoalCombinatorType.NONE, mutableListOf(createEqualGoal(goalEntity)), mutableListOf())
            }
            is ConstraintEntity -> {
                Frame(GoalCombinatorType.NONE, mutableListOf(createConstraintGoal(goalEntity)), mutableListOf())
            }
            is AndEntity -> Frame(
                GoalCombinatorType.AND, mutableListOf(), goalEntity.goals.toMutableList()
            )
            is OrEntity -> Frame(
                GoalCombinatorType.OR, mutableListOf(), goalEntity.goals.toMutableList()
            )
        }
    }

    @FlowPreview
    private fun finalizeCurrentFrame(currentFrame: Frame, finalFrame: Frame, stack: ArrayDeque<Frame>) {
        when (currentFrame.currentGoalEntity) {
            GoalCombinatorType.NONE -> finalFrame.goals.add(currentFrame.goals.last())
            GoalCombinatorType.AND -> {
                val goal = Logikal.and(currentFrame.goals)
                if (stack.isEmpty()) {
                    finalFrame.goals.add(goal)
                } else {
                    val previousFrame = stack.last()
                    previousFrame.goals.add(goal)
                }
            }
            GoalCombinatorType.OR -> {
                val goal = Logikal.or(currentFrame.goals)
                if (stack.isEmpty()) {
                    finalFrame.goals.add(goal)
                } else {
                    val previousFrame = stack.last()
                    previousFrame.goals.add(goal)
                }
            }
        }
    }

    private fun processNextGoalEntity(currentFrame: Frame, stack: ArrayDeque<Frame>) {
        when (val goalEntity = currentFrame.goalEntities.removeLast()) {
            is EqualEntity -> {
                currentFrame.goals.add(createEqualGoal(goalEntity))
                stack.addLast(currentFrame)
            }
            is ConstraintEntity -> {
                currentFrame.goals.add(createConstraintGoal(goalEntity))
                stack.addLast(currentFrame)
            }
            is AndEntity -> {
                stack.addLast(currentFrame)
                val nextFrame = Frame(
                    GoalCombinatorType.AND, mutableListOf(), goalEntity.goals.toMutableList()
                )
                stack.addLast(nextFrame)
            }
            is OrEntity -> {
                stack.addLast(currentFrame)
                val nextFrame = Frame(
                    GoalCombinatorType.OR, mutableListOf(), goalEntity.goals.toMutableList()
                )
                stack.addLast(nextFrame)
            }
        }
    }

    private fun createEqualGoal(equalEntity: EqualEntity): GoalFun {
        val firstValue = ValueConverter.convertToValue(equalEntity.firstValueEntity)
        val secondValue = ValueConverter.convertToValue(equalEntity.secondValueEntity)
        return Logikal.equal(firstValue, secondValue)
    }

    private fun createConstraintGoal(constraintEntity: ConstraintEntity): GoalFun {
        return if (constraintEntity.constraintGoal != null) {
            constraintEntity.constraintGoal
        } else {
            val constraintFun = constraintRegistry[constraintEntity.constraintName]
                ?: error("Following constraint doesn't exist: ${constraintEntity.constraintName} !")
            val goalParameterValues = constraintEntity.parameters
                .map(ValueConverter::convertToValue)
                .toTypedArray()
            val evaluatedConstraintEntity = constraintFun.call(*goalParameterValues) as ConstraintEntity
            evaluatedConstraintEntity.constraintGoal!!
        }
    }
}
