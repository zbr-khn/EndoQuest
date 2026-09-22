package com.example.endoquest.model

data class QuizQuestion(
    val id: String,
    val questionText: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String,
    val difficulty: String = "Beginner",
    val category: String = "General Endodontics",
    val coinReward: Int = 100
)
