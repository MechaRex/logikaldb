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

import com.logikaldb.ConstraintRegistryTyped
import com.logikaldb.entity.AndEntityV2
import com.logikaldb.entity.ConstraintEntityV2
import com.logikaldb.entity.EqualEntityV2
import com.logikaldb.entity.GoalTypedEntity
import com.logikaldb.entity.GoalV2
import com.logikaldb.entity.OrEntityV2
import com.logikaldb.logikal.GoalTyped
import com.logikaldb.logikal.LogikalTyped
import kotlinx.coroutines.FlowPreview

internal class GoalTypedConverter(private val constraintRegistry: ConstraintRegistryTyped) {
    enum class GoalCombinatorType { NONE, AND, OR }
    data class Frame(val currentGoalEntity: GoalCombinatorType, val goals: MutableList<GoalTyped>, val goalEntities: MutableList<GoalV2>)

    @FlowPreview
    fun convertToGoal(goalEntity: GoalTypedEntity): GoalTyped {
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

    private fun createInitialFrame(goalEntity: GoalV2): Frame {
        return when (goalEntity) {
            is EqualEntityV2 -> {
                Frame(GoalCombinatorType.NONE, mutableListOf(createEqualGoal(goalEntity)), mutableListOf())
            }
            is ConstraintEntityV2 -> {
                Frame(GoalCombinatorType.NONE, mutableListOf(createConstraintGoal(goalEntity)), mutableListOf())
            }
            is AndEntityV2 -> Frame(
                GoalCombinatorType.AND, mutableListOf(), goalEntity.goals.toMutableList()
            )
            is OrEntityV2 -> Frame(
                GoalCombinatorType.OR, mutableListOf(), goalEntity.goals.toMutableList()
            )
        }
    }

    @FlowPreview
    private fun finalizeCurrentFrame(currentFrame: Frame, finalFrame: Frame, stack: ArrayDeque<Frame>) {
        when (currentFrame.currentGoalEntity) {
            GoalCombinatorType.NONE -> finalFrame.goals.add(currentFrame.goals.last())
            GoalCombinatorType.AND -> {
                val goal = LogikalTyped.and(currentFrame.goals)
                if (stack.isEmpty()) {
                    finalFrame.goals.add(goal)
                } else {
                    val previousFrame = stack.last()
                    previousFrame.goals.add(goal)
                }
            }
            GoalCombinatorType.OR -> {
                val goal = LogikalTyped.or(currentFrame.goals)
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
            is EqualEntityV2 -> {
                currentFrame.goals.add(createEqualGoal(goalEntity))
                stack.addLast(currentFrame)
            }
            is ConstraintEntityV2 -> {
                currentFrame.goals.add(createConstraintGoal(goalEntity))
                stack.addLast(currentFrame)
            }
            is AndEntityV2 -> {
                stack.addLast(currentFrame)
                val nextFrame = Frame(
                    GoalCombinatorType.AND, mutableListOf(), goalEntity.goals.toMutableList()
                )
                stack.addLast(nextFrame)
            }
            is OrEntityV2 -> {
                stack.addLast(currentFrame)
                val nextFrame = Frame(
                    GoalCombinatorType.OR, mutableListOf(), goalEntity.goals.toMutableList()
                )
                stack.addLast(nextFrame)
            }
        }
    }

    private fun createEqualGoal(equalEntity: EqualEntityV2): GoalTyped {
        val firstValue = ValueTypedConverter.convertToValue(equalEntity.firstValueEntity)
        val secondValue = ValueTypedConverter.convertToValue(equalEntity.secondValueEntity)
        return LogikalTyped.equal(firstValue, secondValue)
    }

    private fun createConstraintGoal(constraintEntity: ConstraintEntityV2): GoalTyped {
        return if (constraintEntity.constraintGoal != null) {
            constraintEntity.constraintGoal
        } else {
            val constraintFun = constraintRegistry[constraintEntity.constraintName]
                ?: error("Following constraint doesn't exist: ${constraintEntity.constraintName} !")
            val goalParameterValues = constraintEntity.parameters
                .map(ValueTypedConverter::convertToValue)
                .toTypedArray()
            val evaluatedConstraintEntity = constraintFun.call(*goalParameterValues) as ConstraintEntityV2
            evaluatedConstraintEntity.constraintGoal!!
        }
    }
}
