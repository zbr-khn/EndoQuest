package com.example.endoquest.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.endoquest.audio.SoundManager
import com.example.endoquest.data.SampleData
import com.example.endoquest.model.PlayerProgress
import com.example.endoquest.model.QuizQuestion
import com.example.endoquest.model.RCTStage
import com.example.endoquest.model.RunQuizQuestion

class GameViewModel(application: Application) : AndroidViewModel(application) {
    val soundManager = SoundManager(application)

    var progress by mutableStateOf(PlayerProgress())
        private set

    var currentRunnerQuizIndex by mutableStateOf(0)
        private set

    var currentRctStageIndex by mutableStateOf(0)
        private set

    val runnerQuestions: List<QuizQuestion> = SampleData.runEndodonticsQuestionPool.map { runQ ->
        val correctIndex = runQ.rawOptions.indexOf(runQ.correctAnswerText).coerceAtLeast(0)
        QuizQuestion(
            id = runQ.id,
            questionText = runQ.questionText,
            options = runQ.rawOptions,
            correctAnswerIndex = correctIndex,
            explanation = runQ.explanation,
            difficulty = runQ.difficulty,
            category = runQ.category
        )
    }
    val rctStages: List<RCTStage> = SampleData.rctStages

    // 4th-Year BDS Endodontics MCQ Pool for Runner Checkpoints
    val runQuestionPool: List<RunQuizQuestion> = SampleData.runEndodonticsQuestionPool
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

    // Runner Lane and Action States
    var playerLane by mutableStateOf(1) // 0: Left, 1: Center, 2: Right
        private set

    var isJumping by mutableStateOf(false)
        private set

    var isSliding by mutableStateOf(false)
        private set

    // Goal reached celebration popup state
    var showGoalReachedPopup by mutableStateOf(false)
        private set

    // Dental Police Checkpoint encounter states
    var isPoliceEncounterActive by mutableStateOf(false)
        private set

    var policeSprintProgress by mutableStateOf(0f)
        private set

    var policeSaluting by mutableStateOf(false)
        private set

    var lastPoliceMilestone by mutableStateOf(0)
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
        if (!isPoliceEncounterActive && !isJumping && !isSliding) {
            isJumping = true
            soundManager.playJump()
        }
    }

    fun slide() {
        if (!isPoliceEncounterActive && !isJumping && !isSliding) {
            isSliding = true
            soundManager.playSlide()
        }
    }

    var runnerResetTrigger by mutableStateOf(0)
        private set

    fun resetActionState() {
        isJumping = false
        isSliding = false
    }

    fun resetJump() {
        isJumping = false
    }

    fun resetRunner() {
        resetActionState()
        runnerResetTrigger++
    }

    fun addMoney(amount: Int) {
        val newAmount = progress.moneyCollected + amount
        progress = progress.copy(moneyCollected = newAmount)
        soundManager.playCoinCollection()
        
        if (newAmount >= progress.targetMoney) {
            showGoalReachedPopup = true
            soundManager.playQuizSuccess()
        } else {
            checkPoliceCheckpoint()
        }
    }

    private fun checkPoliceCheckpoint() {
        val milestoneStep = 500
        val currentMilestone = (progress.moneyCollected / milestoneStep) * milestoneStep
        if (currentMilestone > 0 && currentMilestone > lastPoliceMilestone && progress.moneyCollected < progress.targetMoney) {
            lastPoliceMilestone = currentMilestone
            triggerPoliceEncounter()
        }
    }

    fun triggerPoliceEncounter() {
        isPoliceEncounterActive = true
        policeSprintProgress = 0f
        policeSaluting = false

        if (currentRunQuestion != null && runCheckpointPassed != true) {
            selectedRunOptionIndex = null
            runCheckpointPassed = null
        } else {
            val available = runQuestionPool.filter { it.id !in usedRunQuestionIds }
            val picked = if (available.isNotEmpty()) {
                available.random()
            } else {
                usedRunQuestionIds.clear()
                runQuestionPool.random()
            }
            usedRunQuestionIds.add(picked.id)
            currentRunQuestion = picked
            currentRunShuffledOptions = picked.rawOptions.shuffled()
            selectedRunOptionIndex = null
            runCheckpointPassed = null
        }
        soundManager.playPoliceSiren()
    }

    fun updatePoliceSprint(progress: Float) {
        policeSprintProgress = progress.coerceIn(0f, 1f)
    }

    fun submitRunQuizAnswer(optionIndex: Int) {
        val q = currentRunQuestion ?: return
        if (runCheckpointPassed == true) return

        selectedRunOptionIndex = optionIndex
        val selectedText = currentRunShuffledOptions.getOrNull(optionIndex) ?: return
        val isCorrect = selectedText == q.correctAnswerText
        runCheckpointPassed = isCorrect

        val newCorrect = if (isCorrect) progress.correctQuizAnswers + 1 else progress.correctQuizAnswers
        val newTotal = progress.totalQuizAttempts + 1
        val accuracy = if (newTotal > 0) newCorrect.toFloat() / newTotal.toFloat() else 0f

        if (isCorrect) {
            soundManager.playQuizSuccess()
            policeSaluting = true
            val updatedMoney = progress.moneyCollected + q.coinReward
            if (updatedMoney >= progress.targetMoney) {
                showGoalReachedPopup = true
            }
            progress = progress.copy(
                moneyCollected = updatedMoney,
                correctQuizAnswers = newCorrect,
                totalQuizAttempts = newTotal,
                rctQuizAccuracy = accuracy
            )
        } else {
            soundManager.playQuizError()
            policeSaluting = false
            progress = progress.copy(
                correctQuizAnswers = newCorrect,
                totalQuizAttempts = newTotal,
                rctQuizAccuracy = accuracy
            )
        }
    }

    fun answerPoliceQuiz(isCorrect: Boolean) {
        val q = currentRunQuestion
        if (q != null && currentRunShuffledOptions.isNotEmpty()) {
            val targetIndex = if (isCorrect) {
                currentRunShuffledOptions.indexOf(q.correctAnswerText).takeIf { it >= 0 } ?: 0
            } else {
                currentRunShuffledOptions.indexOfFirst { it != q.correctAnswerText }.takeIf { it >= 0 } ?: 0
            }
            submitRunQuizAnswer(targetIndex)
        }
    }

    fun retryRunQuestion() {
        selectedRunOptionIndex = null
        runCheckpointPassed = null
        policeSaluting = false
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
        currentRunnerQuizIndex = 0
        currentRctStageIndex = 0
        playerLane = 1
        isJumping = false
        isSliding = false
        showGoalReachedPopup = false
        isPoliceEncounterActive = false
        policeSprintProgress = 0f
        policeSaluting = false
        lastPoliceMilestone = 0
        usedRunQuestionIds.clear()
        currentRunQuestion = null
        currentRunShuffledOptions = emptyList()
        selectedRunOptionIndex = null
        runCheckpointPassed = null
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }
}
