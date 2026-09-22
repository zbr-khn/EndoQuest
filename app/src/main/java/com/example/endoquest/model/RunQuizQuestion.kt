package com.example.endoquest.model

/**
 * Dedicated data model for Endodontic Multiple Choice Questions used during the 3D Runner checkpoints.
 * Strictly separated from the clinical RCT Stage simulation questions.
 */
data class RunQuizQuestion(
    val id: String,
    val questionText: String,
    val rawOptions: List<String>,
    val correctAnswerText: String,
    val explanation: String,
    val difficulty: String = "4th-Year BDS",
    val category: String = "Endodontics",
    val coinReward: Int = 50
)
