package com.example.endoquest.model

data class RCTStage(
    val stageNumber: Int,
    val title: String,
    val educationalDescription: String,
    val quizQuestion: QuizQuestion,
    val procedurePlaceholderName: String
)
