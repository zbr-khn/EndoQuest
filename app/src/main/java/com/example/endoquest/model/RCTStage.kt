package com.example.endoquest.model

/**
 * Data model for Clinical RCT Procedure Simulation stages.
 * Zero quiz questions: stages are exclusively hands-on clinical procedures operated via interactive slider.
 */
data class RCTStage(
    val stageNumber: Int,
    val title: String,
    val educationalDescription: String,
    val procedurePlaceholderName: String
)
