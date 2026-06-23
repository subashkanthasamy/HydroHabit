package com.bose.hydrohabit.domain.engine

import com.bose.hydrohabit.domain.model.ActivityLevel
import com.bose.hydrohabit.domain.model.Gender
import com.bose.hydrohabit.domain.model.GoalSource
import com.bose.hydrohabit.testutil.sampleProfile
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HydrationGoalCalculatorTest {

    private val calculator = HydrationGoalCalculator()
    private val date = LocalDate(2026, 6, 18)

    @Test
    fun baseGoalForModerateMale() {
        // 70kg * 35 * 1.0(age<=30) * 1.0(male) = 2450 base, * 1.2 moderate = 2940
        val goal = calculator.calculate(sampleProfile(weightKg = 70.0), date)
        assertEquals(2450, goal.baseMl)
        assertEquals(2940, goal.targetMl)
        assertEquals(490, goal.activityAdjustmentMl)
        assertEquals(GoalSource.CALCULATED, goal.source)
    }

    @Test
    fun higherActivityYieldsHigherGoal() {
        val moderate = calculator.calculate(sampleProfile(activityLevel = ActivityLevel.MODERATE), date)
        val veryActive = calculator.calculate(sampleProfile(activityLevel = ActivityLevel.VERY_ACTIVE), date)
        assertTrue(veryActive.targetMl > moderate.targetMl)
    }

    @Test
    fun femaleFactorLowersGoal() {
        val male = calculator.calculate(sampleProfile(gender = Gender.MALE), date)
        val female = calculator.calculate(sampleProfile(gender = Gender.FEMALE), date)
        assertTrue(female.targetMl < male.targetMl)
    }

    @Test
    fun goalIsClampedToSafeMaximum() {
        val goal = calculator.calculate(
            sampleProfile(weightKg = 200.0, activityLevel = ActivityLevel.VERY_ACTIVE),
            date,
        )
        assertEquals(4000, goal.targetMl)
    }

    @Test
    fun goalIsClampedToSafeMinimum() {
        val goal = calculator.calculate(
            sampleProfile(weightKg = 30.0, activityLevel = ActivityLevel.SEDENTARY),
            date,
        )
        assertEquals(1500, goal.targetMl)
    }

    @Test
    fun weatherAdjustmentIsAddedAndRecorded() {
        val goal = calculator.calculate(sampleProfile(weightKg = 70.0), date, weatherAdjustmentMl = 300)
        assertEquals(300, goal.weatherAdjustmentMl)
        assertEquals(2940 + 300, goal.targetMl)
    }
}
