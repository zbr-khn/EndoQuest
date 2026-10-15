package com.example.endoquest.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.endoquest.audio.SoundManager
import com.example.endoquest.data.EndoQuestion
import com.example.endoquest.data.QuestionRepository
import com.example.endoquest.data.SampleData
import com.example.endoquest.model.PlayerProgress
import com.example.endoquest.model.QuizQuestion
import com.example.endoquest.model.RCTStage
import com.example.endoquest.model.RoundOverInfo
import com.example.endoquest.model.RunQuizQuestion
import java.util.LinkedList
import java.util.Queue
import kotlin.math.abs

class GameViewModel(application: Application) : AndroidViewModel(application) {
    val soundManager = SoundManager(application)

    var progress by mutableStateOf(PlayerProgress())
        private set

    // =========================================================================
    // PER-RUN RANDOMIZED QUESTION PARTITION (Questions 1–20 from QuestionRepository)
    // =========================================================================
    var currentRunQuestionSet by mutableStateOf(QuestionRepository.createRandomizedRunSet())
        private set

    // Legacy fallback list for QuizScreen
    val runnerQuestions: List<QuizQuestion>
        get() = currentRunQuestionSet.allQuestions.map { it.toQuizQuestion(shuffleOptions = true) }

    // =========================================================================
    // ROUND 1: RUNNER + DENTAL POLICE STATE (15s intervals, exactly 3 questions, +$500)
    // =========================================================================
    var round1QuestionCount by mutableIntStateOf(0)
        private set
    val maxRound1Questions = 3
    val round1QuestionReward = 500

    var isRound1Complete by mutableStateOf(false)
        private set

    val runQuestionPool: List<RunQuizQuestion>
        get() = currentRunQuestionSet.round1RunnerQuestions.map { it.toRunQuizQuestion(shuffleOptions = false) }

    val usedRunQuestionIds = mutableSetOf<String>()

    var currentRunQuestion by mutableStateOf<RunQuizQuestion?>(null)
        private set
    var currentRunShuffledOptions by mutableStateOf<List<String>>(emptyList())
        private set
    var selectedRunOptionIndex by mutableStateOf<Int?>(null)
        private set
    var runCheckpointPassed by mutableStateOf<Boolean?>(null)
        private set

    val policeInspectionPassed: Boolean?
        get() = runCheckpointPassed

    var playerLane by mutableStateOf(1) // 0: Left, 1: Center, 2: Right
        private set
    var isJumping by mutableStateOf(false)
        private set
    var isSliding by mutableStateOf(false)
        private set
    var jumpTrigger by mutableIntStateOf(0)
        private set
    var slideTrigger by mutableIntStateOf(0)
        private set

    var isPoliceEncounterActive by mutableStateOf(false)
        private set
    var policeSprintProgress by mutableStateOf(0f)
        private set
    var policeSaluting by mutableStateOf(false)
        private set

    var runnerResetTrigger by mutableIntStateOf(0)
        private set

    var isMuted by mutableStateOf(soundManager.isMuted)
        private set

    fun toggleAudioMute(): Boolean {
        val muted = soundManager.toggleMute()
        isMuted = muted
        return muted
    }

    fun moveLeft() {
        if (!isPoliceEncounterActive && playerLane > 0) {
            playerLane--
            soundManager.playFootstep()
        }
    }

    fun moveRight() {
        if (!isPoliceEncounterActive && playerLane < 2) {
            playerLane++
            soundManager.playFootstep()
        }
    }

    fun jump() {
        if (!isPoliceEncounterActive) {
            isSliding = false
            isJumping = true
            jumpTrigger++
            soundManager.playJump()
        }
    }

    fun slide() {
        if (!isPoliceEncounterActive) {
            isJumping = false
            isSliding = true
            slideTrigger++
            soundManager.playSlide()
        }
    }

    fun resetActionState() {
        isJumping = false
        isSliding = false
    }

    fun resetJump() {
        isJumping = false
    }

    fun resetSlide() {
        isSliding = false
    }

    fun resetRunner() {
        resetActionState()
        runnerResetTrigger++
    }

    /**
     * Adds coin money collected during runner (always $25 each)
     */
    fun addCoinMoney(amount: Int = 25) {
        val newCoinTotal = progress.round1CoinEarnings + amount
        val newTotal = progress.moneyCollected + amount
        progress = progress.copy(
            round1CoinEarnings = newCoinTotal,
            moneyCollected = newTotal
        )
        soundManager.playCoinCollection()
    }

    fun addMoney(amount: Int) {
        addCoinMoney(amount)
    }

    fun triggerPoliceEncounter() {
        if (round1QuestionCount >= maxRound1Questions) return

        isPoliceEncounterActive = true
        policeSprintProgress = 0f
        policeSaluting = false

        // Pick next question from randomized Round 1 pool
        val pool = currentRunQuestionSet.round1RunnerQuestions
        val qIndex = round1QuestionCount.coerceIn(0, pool.size - 1)
        val endoQ = pool[qIndex]
        val shuffledOptions = endoQ.options.shuffled()

        currentRunQuestion = endoQ.toRunQuizQuestion(false)
        currentRunShuffledOptions = shuffledOptions.map { it.text }
        selectedRunOptionIndex = null
        runCheckpointPassed = null

        soundManager.playPoliceSiren()
    }

    fun updatePoliceSprint(p: Float) {
        policeSprintProgress = p.coerceIn(0f, 1f)
    }

    fun submitRunQuizAnswer(optionIndex: Int, onRoundOver: () -> Unit) {
        val q = currentRunQuestion ?: return
        if (runCheckpointPassed == true) return

        selectedRunOptionIndex = optionIndex
        val selectedText = currentRunShuffledOptions.getOrNull(optionIndex) ?: return
        // Validates directly against stored answer text (independent of option letter or index)
        val isCorrect = selectedText == q.correctAnswerText
        runCheckpointPassed = isCorrect

        val newTotalAttempts = progress.totalQuizAttempts + 1

        if (isCorrect) {
            soundManager.playQuizSuccess()
            policeSaluting = true
            round1QuestionCount++
            val newCorrect = progress.correctQuizAnswers + 1
            val newR1QuestionsCorrect = progress.round1QuestionsCorrect + 1
            val newMoney = progress.moneyCollected + round1QuestionReward
            val r1Total = progress.round1CoinEarnings + (newR1QuestionsCorrect * round1QuestionReward)

            progress = progress.copy(
                moneyCollected = newMoney,
                round1QuestionsCorrect = newR1QuestionsCorrect,
                round1TotalMoney = r1Total,
                correctQuizAnswers = newCorrect,
                totalQuizAttempts = newTotalAttempts,
                rctQuizAccuracy = newCorrect.toFloat() / newTotalAttempts.toFloat()
            )

            if (round1QuestionCount >= maxRound1Questions) {
                isRound1Complete = true
                progress = progress.copy(round1TotalMoney = progress.moneyCollected)
            }
        } else {
            soundManager.playQuizError()
            policeSaluting = false
            progress = progress.copy(
                totalQuizAttempts = newTotalAttempts,
                rctQuizAccuracy = progress.correctQuizAnswers.toFloat() / newTotalAttempts.toFloat()
            )

            // WRONG ANSWER IN ROUND 1: Triggers Round Over
            triggerRoundOver(
                round = 1,
                title = "Round 1 Over",
                subtitle = "Dental Highway Inspection Failed!",
                reason = "Incorrect answer to Officer Floss's clinical inspection.",
                explanation = q.explanation,
                targetRoute = "runner"
            )
            onRoundOver()
        }
    }

    fun resumeRunnerFromPolice() {
        isPoliceEncounterActive = false
        policeSprintProgress = 0f
        policeSaluting = false
        currentRunQuestion = null
        currentRunShuffledOptions = emptyList()
        selectedRunOptionIndex = null
        runCheckpointPassed = null
    }

    // =========================================================================
    // ROUND 2: THREE-DOOR DENTAL MAZE WITH PAC-MAN BACTERIUM CHASE
    // =========================================================================
    val requiredRound2Money: Int
        get() = (3500 - progress.round1TotalMoney).coerceAtLeast(0)

    var playerMazeX by mutableIntStateOf(1)
        private set
    var playerMazeY by mutableIntStateOf(1)
        private set

    // Bacterium pursuer position (starts at center corridor)
    var bacteriumX by mutableIntStateOf(5)
        private set
    var bacteriumY by mutableIntStateOf(5)
        private set
    var isBacteriumActive by mutableStateOf(false)
        private set
    var bacteriumWarningActive by mutableStateOf(false)
        private set

    val mazeQuestions: List<RunQuizQuestion>
        get() = currentRunQuestionSet.round2MazeQuestions.map { it.toRunQuizQuestion(false) }

    val mazeCheckpointsSolved = mutableSetOf<Int>() // 0, 1, 2 for the 3 doors

    var currentMazeCheckpointIndex by mutableIntStateOf(-1)
        private set
    var currentMazeQuestion by mutableStateOf<RunQuizQuestion?>(null)
        private set
    var currentMazeShuffledOptions by mutableStateOf<List<String>>(emptyList())
        private set
    var selectedMazeOptionIndex by mutableStateOf<Int?>(null)
        private set
    var isMazeCheckpointActive by mutableStateOf(false)
        private set
    var mazeTimerSeconds by mutableIntStateOf(15)
        private set
    var isRound2Complete by mutableStateOf(false)
        private set

    fun moveMaze(
        dx: Int,
        dy: Int,
        mazeGrid: Array<IntArray>,
        onTriggerCheckpoint: (Int) -> Unit,
        onReachExit: () -> Unit
    ) {
        if (isMazeCheckpointActive || isRound2Complete) return

        val newX = playerMazeX + dx
        val newY = playerMazeY + dy

        if (newY in mazeGrid.indices && newX in mazeGrid[0].indices) {
            val cell = mazeGrid[newY][newX]
            // 0 = Corridor, 1 = Enamel Wall, 2 = Door 1, 3 = Door 2, 4 = Door 3, 9 = Exit Portal
            if (cell != 1) {
                playerMazeX = newX
                playerMazeY = newY
                soundManager.playFootstep()

                // Check doors
                if (cell == 2 && 0 !in mazeCheckpointsSolved) {
                    openMazeCheckpoint(0)
                    onTriggerCheckpoint(0)
                } else if (cell == 3 && 1 !in mazeCheckpointsSolved) {
                    openMazeCheckpoint(1)
                    onTriggerCheckpoint(1)
                } else if (cell == 4 && 2 !in mazeCheckpointsSolved) {
                    openMazeCheckpoint(2)
                    onTriggerCheckpoint(2)
                } else if (cell == 9) {
                    // Requires all 3 doors unlocked
                    if (mazeCheckpointsSolved.size >= 3) {
                        completeRound2()
                        onReachExit()
                    }
                }
            }
        }
    }

    fun activateBacterium() {
        isBacteriumActive = true
    }

    /**
     * Executes one step of Pac-Man-style Breadth-First-Search (BFS) pathfinding
     * for the dental bacterium through valid corridors respecting maze walls.
     */
    fun stepBacteriumTowardsPlayer(mazeGrid: Array<IntArray>, onCatchPlayer: () -> Unit) {
        if (!isBacteriumActive || isMazeCheckpointActive || isRound2Complete) return

        val targetX = playerMazeX
        val targetY = playerMazeY
        val startX = bacteriumX
        val startY = bacteriumY

        if (startX == targetX && startY == targetY) {
            triggerBacteriumCatch(onCatchPlayer)
            return
        }

        // BFS pathfinding to find the next move
        val rows = mazeGrid.size
        val cols = mazeGrid[0].size
        val visited = Array(rows) { BooleanArray(cols) }
        val parent = mutableMapOf<Pair<Int, Int>, Pair<Int, Int>>()
        val queue: Queue<Pair<Int, Int>> = LinkedList()

        val startNode = Pair(startX, startY)
        queue.add(startNode)
        visited[startY][startX] = true

        val directions = listOf(Pair(0, -1), Pair(0, 1), Pair(-1, 0), Pair(1, 0))
        var found = false

        while (queue.isNotEmpty()) {
            val curr = queue.poll() ?: break
            if (curr.first == targetX && curr.second == targetY) {
                found = true
                break
            }

            for (dir in directions) {
                val nx = curr.first + dir.first
                val ny = curr.second + dir.second

                if (ny in 0 until rows && nx in 0 until cols && !visited[ny][nx]) {
                    val cell = mazeGrid[ny][nx]
                    if (cell != 1) { // Walkable corridor / door / portal
                        visited[ny][nx] = true
                        val nextNode = Pair(nx, ny)
                        parent[nextNode] = curr
                        queue.add(nextNode)
                    }
                }
            }
        }

        if (found) {
            // Reconstruct path backward to get immediate next step from start
            var step: Pair<Int, Int>? = Pair(targetX, targetY)
            var nextStep = step

            while (step != null && parent[step] != startNode && parent[step] != null) {
                step = parent[step]
                nextStep = step
            }

            if (step != null && parent[step] == startNode) {
                bacteriumX = step.first
                bacteriumY = step.second
            } else if (nextStep != null) {
                bacteriumX = nextStep.first
                bacteriumY = nextStep.second
            }
        }

        // Check if bacterium caught player
        if (bacteriumX == playerMazeX && bacteriumY == playerMazeY) {
            triggerBacteriumCatch(onCatchPlayer)
        } else {
            // Update proximity warning
            val dist = abs(bacteriumX - playerMazeX) + abs(bacteriumY - playerMazeY)
            bacteriumWarningActive = dist <= 3
        }
    }

    private fun triggerBacteriumCatch(onCatchPlayer: () -> Unit) {
        soundManager.playQuizError()
        triggerRoundOver(
            round = 2,
            title = "Round 2 Over",
            subtitle = "Overtaken by Oral Biofilm!",
            reason = "The dental bacterium pursuer caught you inside the enamel corridors.",
            explanation = "Persistent biofilms rapidly colonize untreated spaces. Swift debridement and safe navigation are paramount.",
            targetRoute = "maze"
        )
        onCatchPlayer()
    }

    fun openMazeCheckpoint(checkpointIndex: Int) {
        currentMazeCheckpointIndex = checkpointIndex
        val pool = currentRunQuestionSet.round2MazeQuestions
        val endoQ = pool.getOrElse(checkpointIndex) { pool.first() }
        val shuffledOptions = endoQ.options.shuffled()

        currentMazeQuestion = endoQ.toRunQuizQuestion(false)
        currentMazeShuffledOptions = shuffledOptions.map { it.text }
        selectedMazeOptionIndex = null
        mazeTimerSeconds = 15
        isMazeCheckpointActive = true
        soundManager.playPoliceSiren()
    }

    fun tickMazeTimer(onRoundOver: () -> Unit) {
        if (!isMazeCheckpointActive) return
        if (mazeTimerSeconds > 1) {
            mazeTimerSeconds--
        } else {
            mazeTimerSeconds = 0
            isMazeCheckpointActive = false
            soundManager.playQuizError()

            triggerRoundOver(
                round = 2,
                title = "Round 2 Over",
                subtitle = "Enamel Maze Time Expired!",
                reason = "Countdown expired before committing to a clinical answer.",
                explanation = "In clinical endodontics, acute decision-making under pressure is critical to save the pulp.",
                targetRoute = "maze"
            )
            onRoundOver()
        }
    }

    fun submitMazeAnswer(optionIndex: Int, onRoundOver: () -> Unit, onCorrect: () -> Unit) {
        val q = currentMazeQuestion ?: return
        if (!isMazeCheckpointActive) return

        selectedMazeOptionIndex = optionIndex
        val selectedText = currentMazeShuffledOptions.getOrNull(optionIndex) ?: return
        val isCorrect = selectedText == q.correctAnswerText
        val newTotalAttempts = progress.totalQuizAttempts + 1

        if (isCorrect) {
            soundManager.playQuizSuccess()
            mazeCheckpointsSolved.add(currentMazeCheckpointIndex)
            val newCorrect = progress.correctQuizAnswers + 1
            progress = progress.copy(
                round2QuestionsCorrect = progress.round2QuestionsCorrect + 1,
                correctQuizAnswers = newCorrect,
                totalQuizAttempts = newTotalAttempts,
                rctQuizAccuracy = newCorrect.toFloat() / newTotalAttempts.toFloat()
            )
            isMazeCheckpointActive = false
            onCorrect()
        } else {
            soundManager.playQuizError()
            isMazeCheckpointActive = false
            progress = progress.copy(
                totalQuizAttempts = newTotalAttempts,
                rctQuizAccuracy = progress.correctQuizAnswers.toFloat() / newTotalAttempts.toFloat()
            )

            triggerRoundOver(
                round = 2,
                title = "Round 2 Over",
                subtitle = "Enamel Maze Clinical Error!",
                reason = "Incorrect answer at maze checkpoint.",
                explanation = q.explanation,
                targetRoute = "maze"
            )
            onRoundOver()
        }
    }

    fun completeRound2() {
        if (isRound2Complete) return
        isRound2Complete = true
        soundManager.playQuizSuccess()

        val dynamicAward = requiredRound2Money
        val newTotal = 3500 // Exactly $3,500 target balance
        progress = progress.copy(
            moneyCollected = newTotal,
            round2DynamicReward = dynamicAward,
            currentRound = 3
        )
    }

    // =========================================================================
    // ROUND 3: ZOMBIE DENTAL CHALLENGE (Brings total from $3,500 to $5,000)
    // =========================================================================
    val requiredRound3Money: Int
        get() = (5000 - progress.moneyCollected).coerceAtLeast(0) // $1,500

    val zombieQuestions: List<RunQuizQuestion>
        get() = currentRunQuestionSet.round3ZombieQuestions.map { it.toRunQuizQuestion(false) }

    var currentZombieIndex by mutableIntStateOf(0)
        private set
    var currentZombieQuestion by mutableStateOf<RunQuizQuestion?>(null)
        private set
    var currentZombieShuffledOptions by mutableStateOf<List<String>>(emptyList())
        private set
    var selectedZombieOptionIndex by mutableStateOf<Int?>(null)
        private set
    var zombieTimerSeconds by mutableIntStateOf(15)
        private set
    var isRound3Complete by mutableStateOf(false)
        private set

    fun initZombieRound() {
        currentZombieIndex = 0
        isRound3Complete = false
        loadZombieQuestion(0)
    }

    fun loadZombieQuestion(index: Int) {
        val pool = currentRunQuestionSet.round3ZombieQuestions
        if (index < pool.size) {
            currentZombieIndex = index
            val endoQ = pool[index]
            val shuffledOptions = endoQ.options.shuffled()

            currentZombieQuestion = endoQ.toRunQuizQuestion(false)
            currentZombieShuffledOptions = shuffledOptions.map { it.text }
            selectedZombieOptionIndex = null
            zombieTimerSeconds = 15
        }
    }

    fun tickZombieTimer(onRoundOver: () -> Unit) {
        if (isRound3Complete) return
        if (zombieTimerSeconds > 1) {
            zombieTimerSeconds--
        } else {
            zombieTimerSeconds = 0
            soundManager.playQuizError()

            triggerRoundOver(
                round = 3,
                title = "Round 3 Over",
                subtitle = "Overcome by Dental Zombies!",
                reason = "Countdown expired before neutralizing the microbial threat.",
                explanation = "Persistent biofilms must be eradicated rapidly with strict chemical protocol.",
                targetRoute = "zombie_challenge"
            )
            onRoundOver()
        }
    }

    fun submitZombieAnswer(
        optionIndex: Int,
        onRoundOver: () -> Unit,
        onNextZombie: () -> Unit,
        onAllCleared: () -> Unit
    ) {
        val q = currentZombieQuestion ?: return
        selectedZombieOptionIndex = optionIndex
        val selectedText = currentZombieShuffledOptions.getOrNull(optionIndex) ?: return
        val isCorrect = selectedText == q.correctAnswerText
        val newTotalAttempts = progress.totalQuizAttempts + 1

        if (isCorrect) {
            soundManager.playQuizSuccess()
            val newCorrect = progress.correctQuizAnswers + 1
            val newZombieCorrect = progress.round3QuestionsCorrect + 1

            // $500 per zombie question (3 questions = $1,500)
            val updatedMoney = (progress.moneyCollected + 500).coerceAtMost(5000)

            progress = progress.copy(
                moneyCollected = updatedMoney,
                round3QuestionsCorrect = newZombieCorrect,
                correctQuizAnswers = newCorrect,
                totalQuizAttempts = newTotalAttempts,
                rctQuizAccuracy = newCorrect.toFloat() / newTotalAttempts.toFloat()
            )

            if (currentZombieIndex + 1 < 3) {
                loadZombieQuestion(currentZombieIndex + 1)
                onNextZombie()
            } else {
                // All 3 zombies defeated! Exactly $5,000 reached!
                isRound3Complete = true
                progress = progress.copy(
                    moneyCollected = 5000,
                    currentRound = 4
                )
                soundManager.playQuizSuccess()
                onAllCleared()
            }
        } else {
            soundManager.playQuizError()
            progress = progress.copy(
                totalQuizAttempts = newTotalAttempts,
                rctQuizAccuracy = progress.correctQuizAnswers.toFloat() / newTotalAttempts.toFloat()
            )

            triggerRoundOver(
                round = 3,
                title = "Round 3 Over",
                subtitle = "Infection Overran the Canal!",
                reason = "Incorrect answer to the zombie microbial challenge.",
                explanation = q.explanation,
                targetRoute = "zombie_challenge"
            )
            onRoundOver()
        }
    }

    // =========================================================================
    // ROUND OVER & RESTART MECHANICS
    // =========================================================================
    var currentRoundOverInfo by mutableStateOf(RoundOverInfo())
        private set

    val roundOverInfo: RoundOverInfo
        get() = currentRoundOverInfo

    fun restartCurrentRound() {
        when (currentRoundOverInfo.roundNumber) {
            1 -> restartRound1()
            2 -> restartRound2()
            3 -> restartRound3()
            else -> restartRound1()
        }
    }

    fun triggerRoundOver(
        round: Int,
        title: String,
        subtitle: String,
        reason: String,
        explanation: String,
        targetRoute: String
    ) {
        currentRoundOverInfo = RoundOverInfo(
            roundNumber = round,
            title = title,
            subtitle = subtitle,
            reason = reason,
            explanation = explanation,
            moneyAtFailure = progress.moneyCollected,
            targetRoute = targetRoute
        )
    }

    fun restartRound1() {
        round1QuestionCount = 0
        isRound1Complete = false
        playerLane = 1
        resetActionState()
        isPoliceEncounterActive = false
        policeSprintProgress = 0f
        policeSaluting = false
        currentRunQuestion = null
        currentRunShuffledOptions = emptyList()
        selectedRunOptionIndex = null
        runCheckpointPassed = null

        progress = progress.copy(
            moneyCollected = 0,
            round1CoinEarnings = 0,
            round1QuestionsCorrect = 0,
            round1TotalMoney = 0
        )
        resetRunner()
    }

    fun restartRound2() {
        playerMazeX = 1
        playerMazeY = 1
        bacteriumX = 5
        bacteriumY = 5
        isBacteriumActive = false
        bacteriumWarningActive = false
        mazeCheckpointsSolved.clear()
        currentMazeCheckpointIndex = -1
        currentMazeQuestion = null
        currentMazeShuffledOptions = emptyList()
        selectedMazeOptionIndex = null
        isMazeCheckpointActive = false
        mazeTimerSeconds = 15
        isRound2Complete = false

        progress = progress.copy(
            moneyCollected = progress.round1TotalMoney,
            round2DynamicReward = 0,
            round2QuestionsCorrect = 0
        )
    }

    fun restartRound3() {
        currentZombieIndex = 0
        currentZombieQuestion = null
        currentZombieShuffledOptions = emptyList()
        selectedZombieOptionIndex = null
        zombieTimerSeconds = 15
        isRound3Complete = false

        progress = progress.copy(
            moneyCollected = 3500,
            round3QuestionsCorrect = 0
        )
        initZombieRound()
    }

    // =========================================================================
    // CLINICAL RCT STAGES (Zero Quiz Questions, Hands-on Procedure Only)
    // =========================================================================
    var currentRctStageIndex by mutableIntStateOf(0)
        private set
    val rctStages: List<RCTStage> = SampleData.rctStages

    fun recordQuizAnswer(isCorrect: Boolean) {
        val newCorrect = if (isCorrect) progress.correctQuizAnswers + 1 else progress.correctQuizAnswers
        val newTotal = progress.totalQuizAttempts + 1
        val accuracy = if (newTotal > 0) newCorrect.toFloat() / newTotal.toFloat() else 0f
        progress = progress.copy(
            correctQuizAnswers = newCorrect,
            totalQuizAttempts = newTotal,
            rctQuizAccuracy = accuracy
        )
        if (isCorrect) {
            soundManager.playQuizSuccess()
        } else {
            soundManager.playQuizError()
        }
    }

    fun advanceRctStage(): Boolean {
        if (currentRctStageIndex < rctStages.size - 1) {
            currentRctStageIndex++
            progress = progress.copy(
                currentRctStageIndex = currentRctStageIndex,
                rctStagesCompleted = currentRctStageIndex
            )
            return true
        } else {
            progress = progress.copy(rctStagesCompleted = rctStages.size)
            return false
        }
    }

    fun resetGame() {
        progress = PlayerProgress()
        round1QuestionCount = 0
        isRound1Complete = false
        usedRunQuestionIds.clear()

        // Generate a completely fresh randomized 20-question run set
        currentRunQuestionSet = QuestionRepository.createRandomizedRunSet()

        restartRound1()
        restartRound2()
        restartRound3()
        currentRctStageIndex = 0
    }

    // =========================================================================
    // LEVEL SKIP ACTIONS (Consistent progression and milestone balances)
    // =========================================================================
    fun skipRunnerRound() {
        val minR1Money = 1500
        val finalMoney = maxOf(progress.moneyCollected, minR1Money)
        progress = progress.copy(
            moneyCollected = finalMoney,
            round1TotalMoney = finalMoney,
            currentRound = 2
        )
        isRound1Complete = true
        soundManager.playQuizSuccess()
    }

    fun skipMazeRound() {
        completeRound2()
    }

    fun skipZombieRound() {
        isRound3Complete = true
        currentRctStageIndex = 0
        progress = progress.copy(
            moneyCollected = 5000,
            currentRound = 4
        )
        soundManager.playQuizSuccess()
    }

    fun skipRctStages() {
        currentRctStageIndex = rctStages.size - 1
        progress = progress.copy(
            rctStagesCompleted = rctStages.size,
            rctQuizAccuracy = 1.0f,
            moneyCollected = 5000
        )
        soundManager.playQuizSuccess()
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }
}
