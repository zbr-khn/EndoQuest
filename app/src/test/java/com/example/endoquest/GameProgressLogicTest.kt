package com.example.endoquest

import com.example.endoquest.model.PlayerProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests validating player progression, academic scoring, currency calculations,
 * and clinical competency grading tiers.
 */
class GameProgressLogicTest {

    @Test
    fun testDefaultProgressState() {
        val progress = PlayerProgress()
        assertEquals(0, progress.moneyCollected)
        assertEquals(5000, progress.targetMoney)
        assertEquals(1, progress.currentRound)
        assertEquals(0, progress.round1CoinEarnings)
        assertEquals(0, progress.round1QuestionsCorrect)
        assertEquals(0, progress.correctQuizAnswers)
        assertEquals(0, progress.totalQuizAttempts)
        assertEquals(0f, progress.rctQuizAccuracy, 0.001f)
    }

    @Test
    fun testCoinEarningsAccumulation() {
        var progress = PlayerProgress()
        val coinDenomination = 25

        // Simulate collecting 8 coins ($200)
        repeat(8) {
            progress = progress.copy(
                moneyCollected = progress.moneyCollected + coinDenomination,
                round1CoinEarnings = progress.round1CoinEarnings + coinDenomination
            )
        }

        assertEquals(200, progress.moneyCollected)
        assertEquals(200, progress.round1CoinEarnings)
        assertEquals(8, progress.round1CoinEarnings / coinDenomination)
    }

    @Test
    fun testDentalHighwayPatrolRewardMath() {
        var progress = PlayerProgress()
        val policeInspectionReward = 500

        // Simulate passing 3 inspections (Officer Floss inspections 1, 2, and 3)
        for (i in 1..3) {
            progress = progress.copy(
                moneyCollected = progress.moneyCollected + policeInspectionReward,
                round1QuestionsCorrect = progress.round1QuestionsCorrect + 1,
                correctQuizAnswers = progress.correctQuizAnswers + 1,
                totalQuizAttempts = progress.totalQuizAttempts + 1
            )
        }

        assertEquals(1500, progress.moneyCollected)
        assertEquals(3, progress.round1QuestionsCorrect)
        assertEquals(3, progress.correctQuizAnswers)
        assertEquals(3, progress.totalQuizAttempts)
    }

    @Test
    fun testQuizAccuracyComputations() {
        // Zero attempts
        var progress = PlayerProgress()
        assertEquals(0f, progress.rctQuizAccuracy, 0.001f)

        // 3 correct out of 4 attempts (75%)
        progress = progress.copy(
            correctQuizAnswers = 3,
            totalQuizAttempts = 4,
            rctQuizAccuracy = 3f / 4f
        )
        assertEquals(0.75f, progress.rctQuizAccuracy, 0.001f)

        // Perfect score (100%)
        progress = progress.copy(
            correctQuizAnswers = 6,
            totalQuizAttempts = 6,
            rctQuizAccuracy = 6f / 6f
        )
        assertEquals(1.0f, progress.rctQuizAccuracy, 0.001f)
    }

    @Test
    fun testClinicalCompetencyGradingTiers() {
        fun computeGrade(accuracy: Float): String = when {
            accuracy >= 0.90f -> "A+ (Honors Distinction / Master Endodontist)"
            accuracy >= 0.80f -> "A (Clinical Competency / Senior Resident)"
            accuracy >= 0.70f -> "B+ (Satisfactory Technique / Dental Graduate)"
            else -> "B (Needs Review / Provisional Pass)"
        }

        assertEquals("A+ (Honors Distinction / Master Endodontist)", computeGrade(1.0f))
        assertEquals("A+ (Honors Distinction / Master Endodontist)", computeGrade(0.92f))
        assertEquals("A (Clinical Competency / Senior Resident)", computeGrade(0.85f))
        assertEquals("A (Clinical Competency / Senior Resident)", computeGrade(0.80f))
        assertEquals("B+ (Satisfactory Technique / Dental Graduate)", computeGrade(0.75f))
        assertEquals("B (Needs Review / Provisional Pass)", computeGrade(0.60f))
        assertEquals("B (Needs Review / Provisional Pass)", computeGrade(0.0f))
    }
}
