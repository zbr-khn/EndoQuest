package com.example.endoquest.model

data class GameResult(
    val moneyCollected: Int,
    val targetMoney: Int,
    val quizAccuracy: Float,
    val rctAccuracy: Float,
    val isCompleted: Boolean
)
