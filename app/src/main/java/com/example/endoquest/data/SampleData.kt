package com.example.endoquest.data

import com.example.endoquest.model.QuizQuestion
import com.example.endoquest.model.RCTStage
import com.example.endoquest.model.RunQuizQuestion

object SampleData {

    // Master 20-Question Pool accessible via RunQuizQuestion and QuizQuestion models
    val runEndodonticsQuestionPool: List<RunQuizQuestion>
        get() = QuestionRepository.masterBank.map { it.toRunQuizQuestion(shuffleOptions = false) }

    val allQuizQuestions: List<QuizQuestion>
        get() = QuestionRepository.masterBank.map { it.toQuizQuestion(shuffleOptions = false) }

    // Dynamic initial partitions for new runs
    val round1Questions: List<RunQuizQuestion>
        get() = QuestionRepository.masterBank.subList(0, 3).map { it.toRunQuizQuestion(shuffleOptions = true) }

    val round2MazeQuestions: List<RunQuizQuestion>
        get() = QuestionRepository.masterBank.subList(3, 6).map { it.toRunQuizQuestion(shuffleOptions = true) }

    val round3ZombieQuestions: List<RunQuizQuestion>
        get() = QuestionRepository.masterBank.subList(6, 9).map { it.toRunQuizQuestion(shuffleOptions = true) }

    // Legacy fallback questions referencing the master bank
    val questionLevel1: QuizQuestion
        get() = QuestionRepository.masterBank[0].toQuizQuestion(shuffleOptions = false)

    val questionLevel2: QuizQuestion
        get() = QuestionRepository.masterBank[4].toQuizQuestion(shuffleOptions = false)

    val questionLevel3: QuizQuestion
        get() = QuestionRepository.masterBank[5].toQuizQuestion(shuffleOptions = false)

    val runnerQuizQuestions: List<QuizQuestion>
        get() = QuestionRepository.masterBank.subList(0, 3).map { it.toQuizQuestion(shuffleOptions = true) }

    // 3 Clinical Stages (Strictly ZERO quiz questions, pure slider-driven clinical simulation)
    val rctStages = listOf(
        RCTStage(
            stageNumber = 1,
            title = "The Pulp Chamber (Tissue & Smear Layer)",
            educationalDescription = "During chemo-mechanical debridement, tissue dissolution and smear layer removal are paramount. Organic pulp debris and the inorganic smear layer require distinct chemical agents: Sodium Hypochlorite (NaOCl) for dissolving organic pulp remnants, and 17% EDTA for demineralizing the inorganic smear layer.",
            procedurePlaceholderName = "Dissolve or Demineralize (NaOCl & EDTA Irrigation)"
        ),
        RCTStage(
            stageNumber = 2,
            title = "The Apical Danger Zone (Chemical Safety & Hazards)",
            educationalDescription = "Irrigation in the apical third demands meticulous chemical safety. Using a side-vented safety needle positioned 2-3 mm short of the apex ensures gentle coronal reflux and avoids catastrophic periapical extrusion (hypochlorite accident). Furthermore, NaOCl and Chlorhexidine must never be mixed directly inside the canal.",
            procedurePlaceholderName = "The Side-Vent Safe Zone (Apical Reflex Protocol)"
        ),
        RCTStage(
            stageNumber = 3,
            title = "The Inter-Appointment Vault (Medicaments)",
            educationalDescription = "Intracanal medicaments placed between appointments eradicate persisting microbes, neutralize lipopolysaccharides, and alleviate acute symptoms. Calcium Hydroxide [Ca(OH)₂] provides prolonged antibacterial action via high alkaline pH, Ledermix treats severe irreversible pulpitis flare-ups, and Metapex serves as a resorbable filler in primary pulpectomy.",
            procedurePlaceholderName = "Match the Medicament (Ca(OH)₂ Canal Placement)"
        )
    )
}
