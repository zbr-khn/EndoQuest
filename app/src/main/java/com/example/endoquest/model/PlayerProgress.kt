package com.example.endoquest.model

data class PlayerProgress(
    val moneyCollected: Int = 0,
    val targetMoney: Int = 5000,
    val currentRound: Int = 1,
    val round1CoinEarnings: Int = 0,
    val round1QuestionsCorrect: Int = 0,
    val round1TotalMoney: Int = 0,
    val round2DynamicReward: Int = 0,
    val round2QuestionsCorrect: Int = 0,
    val round3QuestionsCorrect: Int = 0,
    val currentRctStageIndex: Int = 0,
    val correctQuizAnswers: Int = 0,
    val totalQuizAttempts: Int = 0,
    val rctStagesCompleted: Int = 0,
    val rctQuizAccuracy: Float = 0f
)
