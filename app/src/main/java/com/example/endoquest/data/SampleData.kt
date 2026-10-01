package com.example.endoquest.data

import com.example.endoquest.model.QuizQuestion
import com.example.endoquest.model.RCTStage

object SampleData {
    // 3 Exact Quick Check Questions with randomized correct answer positions (A, B, C, D)
    val questionLevel1 = QuizQuestion(
        id = "level1_q",
        questionText = "Why does EDTA need to follow NaOCl rather than replace it entirely?",
        options = listOf(
            "A) EDTA causes tooth discolouration",
            "B) EDTA has a higher antibacterial effect than NaOCl",
            "C) EDTA has zero organic tissue dissolving capability",
            "D) EDTA dissolves gutta-percha"
        ),
        correctAnswerIndex = 2, // Option C
        explanation = "EDTA is a chelating agent that removes the inorganic smear layer debris, but has zero organic tissue dissolving capability. Therefore, it must follow NaOCl, which dissolves organic necrotic and vital pulp tissue.",
        difficulty = "Intermediate",
        category = "The Pulp Chamber (Tissue & Smear Layer)",
        coinReward = 100
    )

    val questionLevel2 = QuizQuestion(
        id = "level2_q",
        questionText = "You accidentally mix NaOCl and 2% Chlorhexidine directly inside the canal. What harmful compound precipitates?",
        options = listOf(
            "A) Para-chloroaniline (PCA)",
            "B) Calcium chloride",
            "C) Sodium chloride crystals",
            "D) Urea peroxide"
        ),
        correctAnswerIndex = 0, // Option A
        explanation = "Mixing NaOCl and 2% Chlorhexidine directly inside the canal creates an insoluble brown-orange precipitate of Para-chloroaniline (PCA), which is cytotoxic, potentially carcinogenic, and occludes dentinal tubules.",
        difficulty = "Advanced",
        category = "The Apical Danger Zone (Chemical Safety & Hazards)",
        coinReward = 150
    )

    val questionLevel3 = QuizQuestion(
        id = "level3_q",
        questionText = "How does Calcium Hydroxide primarily destroy bacterial cell membranes inside the root canal?",
        options = listOf(
            "A) By producing extreme acidic pH below 3.0",
            "B) By chelating calcium ions from the dentin",
            "C) By rapid freezing of the pulp space",
            "D) By releasing hydroxyl (OH⁻) ions at an alkaline pH of ~12.5"
        ),
        correctAnswerIndex = 3, // Option D
        explanation = "Calcium Hydroxide [Ca(OH)₂] maintains an intense alkaline pH (~12.5). The release of hydroxyl (OH⁻) ions induces lipid peroxidation, destroying bacterial cellular membranes and denaturing bacterial proteins and DNA.",
        difficulty = "Intermediate",
        category = "The Inter-Appointment Vault (Medicaments)",
        coinReward = 100
    )

    // Highway Checkpoint & Dental Police Quizzes strictly use the user's questions (Legacy fallback)
    val runnerQuizQuestions = listOf(
        questionLevel1,
        questionLevel2,
        questionLevel3
    )

    // Dedicated pool of 9 Endodontics MCQs for 4th-Year BDS students (Runner Checkpoints only)
    val runEndodonticsQuestionPool = listOf(
        com.example.endoquest.model.RunQuizQuestion(
            id = "run_q1",
            questionText = "The primary objective of root canal instrumentation is to:",
            rawOptions = listOf(
                "Remove only the pulp tissue",
                "Eliminate microorganisms and shape the canal for obturation",
                "Enlarge the apical foramen",
                "Remove the periodontal ligament"
            ),
            correctAnswerText = "Eliminate microorganisms and shape the canal for obturation",
            explanation = "The primary objective of root canal instrumentation is eliminating microorganisms and shaping the canal space for a fluid-tight 3D obturation.",
            difficulty = "4th-Year BDS",
            category = "Canal Instrumentation"
        ),
        com.example.endoquest.model.RunQuizQuestion(
            id = "run_q2",
            questionText = "The most commonly used irrigant in root canal treatment is:",
            rawOptions = listOf(
                "Normal saline",
                "Chlorhexidine",
                "Sodium hypochlorite",
                "Hydrogen peroxide"
            ),
            correctAnswerText = "Sodium hypochlorite",
            explanation = "Sodium hypochlorite (NaOCl) is the most commonly used root canal irrigant due to its dual antimicrobial and organic tissue-dissolving properties.",
            difficulty = "4th-Year BDS",
            category = "Root Canal Irrigants"
        ),
        com.example.endoquest.model.RunQuizQuestion(
            id = "run_q3",
            questionText = "The ideal working length terminates approximately at the:",
            rawOptions = listOf(
                "Anatomical apex",
                "Radiographic apex",
                "Apical constriction",
                "Cementoenamel junction"
            ),
            correctAnswerText = "Apical constriction",
            explanation = "The ideal working length terminates at the apical constriction (minor apical diameter), usually 0.5 to 1.0 mm coronal to the radiographic apex.",
            difficulty = "4th-Year BDS",
            category = "Working Length Determination"
        ),
        com.example.endoquest.model.RunQuizQuestion(
            id = "run_q4",
            questionText = "The most commonly used material for the core of root canal obturation is:",
            rawOptions = listOf(
                "Zinc oxide eugenol",
                "Gutta-percha",
                "Glass ionomer cement",
                "Calcium hydroxide"
            ),
            correctAnswerText = "Gutta-percha",
            explanation = "Gutta-percha points combined with an endodontic sealer serve as the primary core material for root canal obturation.",
            difficulty = "4th-Year BDS",
            category = "Obturation Materials"
        ),
        com.example.endoquest.model.RunQuizQuestion(
            id = "run_q5",
            questionText = "Which instrument is primarily used to determine the working length electronically?",
            rawOptions = listOf(
                "Apex locator",
                "Endodontic explorer",
                "Spreader",
                "Plugger"
            ),
            correctAnswerText = "Apex locator",
            explanation = "An electronic apex locator (EAL) uses electrical impedance across multi-frequencies to detect the apical constriction with high accuracy.",
            difficulty = "4th-Year BDS",
            category = "Endodontic Instruments"
        ),
        com.example.endoquest.model.RunQuizQuestion(
            id = "run_q6",
            questionText = "Sodium hypochlorite is particularly useful because of its:",
            rawOptions = listOf(
                "Chelating ability",
                "Tissue-dissolving and antimicrobial properties",
                "Ability to strengthen dentin",
                "Ability to completely remove the smear layer"
            ),
            correctAnswerText = "Tissue-dissolving and antimicrobial properties",
            explanation = "Sodium hypochlorite is uniquely valued in endodontics because it combines broad antimicrobial action with the ability to dissolve vital and necrotic pulp tissue.",
            difficulty = "4th-Year BDS",
            category = "Endodontic Pharmacology"
        ),
        com.example.endoquest.model.RunQuizQuestion(
            id = "run_q7",
            questionText = "The smear layer produced during root canal instrumentation consists mainly of:",
            rawOptions = listOf(
                "Only bacteria",
                "Organic and inorganic debris",
                "Gutta-percha",
                "Salivary proteins only"
            ),
            correctAnswerText = "Organic and inorganic debris",
            explanation = "The endodontic smear layer consists of dentin shavings, cell fragments, pulp remnants, and bacteria, forming a layer of both organic and inorganic debris.",
            difficulty = "4th-Year BDS",
            category = "Smear Layer Biology"
        ),
        com.example.endoquest.model.RunQuizQuestion(
            id = "run_q8",
            questionText = "Which of the following is a common cause of failure of root canal treatment?",
            rawOptions = listOf(
                "Adequate obturation",
                "Proper coronal seal",
                "Persistent intraradicular infection",
                "Correct working length"
            ),
            correctAnswerText = "Persistent intraradicular infection",
            explanation = "Persistent intraradicular infection (such as biofilms in uninstrumented canal recesses) is the primary etiology of endodontic treatment failure.",
            difficulty = "4th-Year BDS",
            category = "Endodontic Pathology & Failure"
        ),
        com.example.endoquest.model.RunQuizQuestion(
            id = "run_q9",
            questionText = "The main purpose of a coronal restoration after RCT is to:",
            rawOptions = listOf(
                "Increase pulp vitality",
                "Prevent coronal leakage and restore function",
                "Increase canal length",
                "Remove the smear layer"
            ),
            correctAnswerText = "Prevent coronal leakage and restore function",
            explanation = "A hermetic coronal restoration prevents coronal microleakage of bacteria/oral fluids into the root canal system and restores the tooth's masticatory function.",
            difficulty = "4th-Year BDS",
            category = "Post-Endodontic Restoration"
        ),
        com.example.endoquest.model.RunQuizQuestion(
            id = "run_q10",
            questionText = "Which microorganism is most frequently isolated from failed root canal cases with persistent periapical lesions?",
            rawOptions = listOf(
                "Streptococcus mutans",
                "Enterococcus faecalis",
                "Lactobacillus acidophilus",
                "Actinomyces viscosus"
            ),
            correctAnswerText = "Enterococcus faecalis",
            explanation = "Enterococcus faecalis is a hardy facultative anaerobe capable of invading dentinal tubules and resisting starvation and alkaline pH, making it the most common isolate in persistent endodontic failures.",
            difficulty = "4th-Year BDS",
            category = "Endodontic Microbiology"
        )
    )

    val round1Questions = listOf(
        runEndodonticsQuestionPool[0], // q1: Instrumentation objective
        runEndodonticsQuestionPool[1], // q2: Irrigant NaOCl
        runEndodonticsQuestionPool[2]  // q3: Working length
    )

    val round2MazeQuestions = listOf(
        runEndodonticsQuestionPool[3], // q4: Gutta-percha
        runEndodonticsQuestionPool[4], // q5: Apex locator
        runEndodonticsQuestionPool[6]  // q7: Smear layer
    )

    val round3ZombieQuestions = listOf(
        runEndodonticsQuestionPool[5], // q6: NaOCl properties
        runEndodonticsQuestionPool[7], // q8: Failure etiology
        runEndodonticsQuestionPool[9], // q10: Enterococcus faecalis
        runEndodonticsQuestionPool[8]  // q9: Coronal restoration
    )


    // 3 Clinical Levels matching the curriculum images
    val rctStages = listOf(
        RCTStage(
            stageNumber = 1,
            title = "The Pulp Chamber (Tissue & Smear Layer)",
            educationalDescription = "During chemo-mechanical debridement, tissue dissolution and smear layer removal are paramount. Organic pulp debris and the inorganic smear layer require distinct chemical agents: Sodium Hypochlorite (NaOCl) for dissolving organic pulp remnants, and 17% EDTA for demineralizing the inorganic smear layer.",
            quizQuestion = questionLevel1,
            procedurePlaceholderName = "Dissolve or Demineralize"
        ),
        RCTStage(
            stageNumber = 2,
            title = "The Apical Danger Zone (Chemical Safety & Hazards)",
            educationalDescription = "Irrigation in the apical third demands meticulous chemical safety. Using a side-vented safety needle positioned 2-3 mm short of the apex ensures gentle coronal reflux and avoids catastrophic periapical extrusion (hypochlorite accident). Furthermore, NaOCl and Chlorhexidine must never be mixed directly inside the canal.",
            quizQuestion = questionLevel2,
            procedurePlaceholderName = "The Side-Vent Safe Zone"
        ),
        RCTStage(
            stageNumber = 3,
            title = "The Inter-Appointment Vault (Medicaments)",
            educationalDescription = "Intracanal medicaments placed between appointments eradicate persisting microbes, neutralize lipopolysaccharides, and alleviate acute symptoms. Calcium Hydroxide [Ca(OH)₂] provides prolonged antibacterial action via high alkaline pH, Ledermix treats severe irreversible pulpitis flare-ups, and Metapex serves as a resorbable filler in primary pulpectomy.",
            quizQuestion = questionLevel3,
            procedurePlaceholderName = "Match the Medicament"
        )
    )
}
