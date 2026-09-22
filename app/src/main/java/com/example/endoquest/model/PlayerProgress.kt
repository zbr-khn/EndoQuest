package com.example.endoquest.model

data class PlayerProgress(
    val moneyCollected: Int = 0,
    val targetMoney: Int = 5000,
    val currentRctStageIndex: Int = 0,
    val correctQuizAnswers: Int = 0,
    val totalQuizAttempts: Int = 0,
    val rctStagesCompleted: Int = 0,
    val rctQuizAccuracy: Float = 0f
)
