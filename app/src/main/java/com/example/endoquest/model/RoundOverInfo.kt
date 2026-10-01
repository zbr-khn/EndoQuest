package com.example.endoquest.model

data class RoundOverInfo(
    val roundNumber: Int = 1,
    val title: String = "Round 1 Over",
    val subtitle: String = "Dental Highway Inspection Failed!",
    val reason: String = "Incorrect clinical answer provided during inspection.",
    val explanation: String = "",
    val moneyAtFailure: Int = 0,
    val targetRoute: String = "runner"
)
