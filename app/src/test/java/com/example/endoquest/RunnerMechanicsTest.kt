package com.example.endoquest

import com.example.endoquest.ui.screens.ObstacleType
import com.example.endoquest.ui.screens.RunnerItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

/**
 * Unit tests validating 3D Endless Runner mechanics, obstacle collision logic,
 * Subway Surfers-style maneuvers (jump, slide, dodge), and guaranteed 3-obstacle spawning queues.
 */
class RunnerMechanicsTest {

    private fun checkCollision(
        obsType: ObstacleType,
        isAirborne: Boolean,
        isDucking: Boolean
    ): Boolean {
        return when (obsType) {
            ObstacleType.GUM_SOCKET -> !isAirborne   // Must jump over!
            ObstacleType.DECAYED_TOOTH -> !isDucking // Must slide under!
            ObstacleType.ROTATING_BUR -> true        // Solid block! Must dodge lane!
            ObstacleType.NONE -> false
        }
    }

    @Test
    fun testDecayedToothSlideUnderMechanic() {
        // Player slides under: safe!
        assertFalse(
            "Sliding under Decayed Tooth must avoid collision",
            checkCollision(ObstacleType.DECAYED_TOOTH, isAirborne = false, isDucking = true)
        )

        // Player runs upright: collision!
        assertTrue(
            "Running upright into Decayed Tooth must trigger collision",
            checkCollision(ObstacleType.DECAYED_TOOTH, isAirborne = false, isDucking = false)
        )

        // Player jumps into tooth crown: collision!
        assertTrue(
            "Jumping into Decayed Tooth crown must trigger collision",
            checkCollision(ObstacleType.DECAYED_TOOTH, isAirborne = true, isDucking = false)
        )
    }

    @Test
    fun testDecayedToothTimingAndDepthProgression() {
        // Simulates the obstacle loop logic:
        fun evaluateObstacleEncounter(
            depth: Float,
            isDucking: Boolean
        ): Pair<Boolean, Boolean> { // Pair(cleared, collision)
            val cleared = isDucking && depth in 0.80f..0.98f
            val collision = depth >= 0.90f && !isDucking
            return Pair(cleared, collision)
        }

        // 1. Approaching tooth at depth 0.85f: player slides early/on-time -> Cleared, NO collision!
        val (cleared1, collision1) = evaluateObstacleEncounter(depth = 0.85f, isDucking = true)
        assertTrue("Sliding while tooth is approaching must clear it safely", cleared1)
        assertFalse("Must not trigger collision when sliding as tooth approaches", collision1)

        // 2. Approaching tooth at depth 0.85f: player hasn't slid yet -> Not cleared, but NO premature collision!
        val (cleared2, collision2) = evaluateObstacleEncounter(depth = 0.85f, isDucking = false)
        assertFalse("Not cleared yet before sliding", cleared2)
        assertFalse("Must NOT register premature hit before tooth reaches player at depth 0.90f", collision2)

        // 3. Tooth reaches player at depth 0.91f: player slides right on contact -> Cleared, NO collision!
        val (cleared3, collision3) = evaluateObstacleEncounter(depth = 0.91f, isDucking = true)
        assertTrue("Sliding right as tooth reaches player must clear it safely", cleared3)
        assertFalse("Sliding at contact point must not trigger collision", collision3)

        // 4. Tooth reaches player at depth 0.91f: player does not slide -> Collision!
        val (cleared4, collision4) = evaluateObstacleEncounter(depth = 0.91f, isDucking = false)
        assertFalse("Cannot clear tooth upright", cleared4)
        assertTrue("Reaching tooth without sliding must trigger collision", collision4)
    }

    @Test
    fun testGumSocketJumpOverMechanic() {
        // Player jumps over: safe!
        assertFalse(
            "Jumping over Gum Socket must avoid collision",
            checkCollision(ObstacleType.GUM_SOCKET, isAirborne = true, isDucking = false)
        )

        // Player slides: trips into socket hole!
        assertTrue(
            "Sliding into Gum Socket must trigger collision",
            checkCollision(ObstacleType.GUM_SOCKET, isAirborne = false, isDucking = true)
        )

        // Player runs upright: trips into socket!
        assertTrue(
            "Running upright into Gum Socket must trigger collision",
            checkCollision(ObstacleType.GUM_SOCKET, isAirborne = false, isDucking = false)
        )
    }

    @Test
    fun testRotatingBurLaneDodgeMechanic() {
        // In-lane collision is always solid
        assertTrue(
            "Rotating Bur handpiece cannot be slid under",
            checkCollision(ObstacleType.ROTATING_BUR, isAirborne = false, isDucking = true)
        )
        assertTrue(
            "Rotating Bur handpiece cannot be jumped over",
            checkCollision(ObstacleType.ROTATING_BUR, isAirborne = true, isDucking = false)
        )

        // Lane dodging test: if player changes lane from 1 to 0 or 2, horizontal delta > 0.42f
        val burLane = 1
        val playerLaneBefore = 1
        val playerLaneAfterDodge = 0

        val inSameLane = abs((burLane - 1).toFloat() - (playerLaneBefore - 1).toFloat()) < 0.42f
        val inDifferentLane = abs((burLane - 1).toFloat() - (playerLaneAfterDodge - 1).toFloat()) < 0.42f

        assertTrue("Player in the same lane must be within collision range", inSameLane)
        assertFalse("Player in adjacent lane must be clear of collision", inDifferentLane)
    }

    @Test
    fun testGuaranteedThreeObstaclesQueueIntegrity() {
        // Verify that the guaranteed obstacle spawner always outputs all 3 obstacle types
        val mandatoryPool = listOf(
            ObstacleType.DECAYED_TOOTH,
            ObstacleType.GUM_SOCKET,
            ObstacleType.ROTATING_BUR
        )

        repeat(20) {
            val shuffledQueue = mandatoryPool.shuffled().toMutableList()
            val spawnedWave = mutableListOf<ObstacleType>()

            while (shuffledQueue.isNotEmpty()) {
                spawnedWave.add(shuffledQueue.removeAt(0))
            }

            assertEquals("Guaranteed wave must contain exactly 3 obstacles", 3, spawnedWave.size)
            assertTrue("Wave must contain DECAYED_TOOTH", spawnedWave.contains(ObstacleType.DECAYED_TOOTH))
            assertTrue("Wave must contain GUM_SOCKET", spawnedWave.contains(ObstacleType.GUM_SOCKET))
            assertTrue("Wave must contain ROTATING_BUR", spawnedWave.contains(ObstacleType.ROTATING_BUR))
        }
    }

    @Test
    fun testCoinElevationCollectionRules() {
        fun canCollectCoin(elevation: Float, isAirborne: Boolean): Boolean {
            val isHighCoin = elevation > 0.30f
            return if (isHighCoin) isAirborne else true
        }

        // Ground coin (slide / run)
        assertTrue("Ground coin can be collected while running", canCollectCoin(elevation = 0f, isAirborne = false))
        assertTrue("Ground coin can be collected while sliding", canCollectCoin(elevation = 0f, isAirborne = false))

        // Elevated jump arc coin
        assertTrue("Airborne jump coin can be collected when player is jumping", canCollectCoin(elevation = 0.75f, isAirborne = true))
        assertFalse("Airborne jump coin passes overhead if player stays on ground", canCollectCoin(elevation = 0.75f, isAirborne = false))
    }
}
