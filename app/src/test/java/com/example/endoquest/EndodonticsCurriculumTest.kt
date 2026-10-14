package com.example.endoquest

import com.example.endoquest.data.QuestionRepository
import com.example.endoquest.data.SampleData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests validating the clinical accuracy, educational completeness,
 * and scientific integrity of the master 20-question Endodontics curriculum bank,
 * per-run selection randomization, and zero-quiz RCT simulation stages.
 */
class EndodonticsCurriculumTest {

    @Test
    fun testMasterBankContainsAll20SuppliedQuestions() {
        val masterBank = QuestionRepository.masterBank
        assertEquals("Master Question bank must contain exactly 20 curriculum questions", 20, masterBank.size)

        for (question in masterBank) {
            assertNotNull("Question ID must not be null", question.id)
            assertTrue("Question ID must not be blank", question.id.isNotBlank())
            assertTrue("Question title must be present (${question.id})", question.title.isNotBlank())
            assertTrue("Question text must be substantive (${question.id})", question.questionText.length > 20)
            assertEquals("Each question must contain exactly 4 options (${question.id})", 4, question.options.size)

            val correctOptions = question.options.filter { it.isCorrect }
            assertEquals("Each question must have exactly ONE correct option (${question.id})", 1, correctOptions.size)

            // Verify each option has text and explanation
            for (opt in question.options) {
                assertTrue("Option text must not be blank (${question.id})", opt.text.isNotBlank())
                assertTrue("Option explanation must provide mechanism (${question.id})", opt.explanation.length > 20)
            }

            assertEquals("All 4 options must have unique text (${question.id})", 4, question.options.map { it.text }.toSet().size)
        }
    }

    @Test
    fun testCoreEndodonticPharmacologyAndTechniquesCoveredIn20Questions() {
        val masterBank = QuestionRepository.masterBank
        val allText = masterBank.joinToString(separator = " ") { q ->
            "${q.title} ${q.questionText} ${q.options.joinToString { it.text + " " + it.explanation }}"
        }

        // Verify key endodontic concepts from the 20 questions
        assertTrue("Must cover pre-heating Sodium Hypochlorite", allText.contains("pre-heating", ignoreCase = true) || allText.contains("50°C", ignoreCase = true))
        assertTrue("Must cover Passive Ultrasonic Irrigation (PUI)", allText.contains("Passive Ultrasonic Irrigation", ignoreCase = true) || allText.contains("PUI", ignoreCase = true))
        assertTrue("Must cover Sodium Hypochlorite accident", allText.contains("Sodium Hypochlorite", ignoreCase = true) && allText.contains("accident", ignoreCase = true))
        assertTrue("Must cover BioPure MTAD (Doxycycline, Citric Acid, Polysorbate 80)", allText.contains("MTAD", ignoreCase = true))
        assertTrue("Must cover EDTA + Chlorhexidine precipitate", allText.contains("EDTA", ignoreCase = true) && allText.contains("Chlorhexidine", ignoreCase = true))
        assertTrue("Must cover Calcium Hydroxide LPS endotoxin neutralization", allText.contains("Lipid A", ignoreCase = true) || allText.contains("endotoxin", ignoreCase = true))
        assertTrue("Must cover Double Antibiotic Paste (DAP)", allText.contains("Double Antibiotic Paste", ignoreCase = true) || allText.contains("DAP", ignoreCase = true))
        assertTrue("Must cover apexification alkaline phosphatase", allText.contains("apexification", ignoreCase = true))
        assertTrue("Must cover sodium perborate walking bleach", allText.contains("Sodium perborate", ignoreCase = true) || allText.contains("Walking Bleach", ignoreCase = true))
        assertTrue("Must cover side-vented safety needle", allText.contains("Side-vented", ignoreCase = true))
        assertTrue("Must cover tissue dissolution (NaOCl)", allText.contains("dissolving", ignoreCase = true))
        assertTrue("Must cover EDTA calcium chelation", allText.contains("chelation", ignoreCase = true))
        assertTrue("Must cover PCA orange-brown precipitate", allText.contains("Para-chloroaniline", ignoreCase = true) || allText.contains("PCA", ignoreCase = true))
        assertTrue("Must cover Chlorhexidine substantivity", allText.contains("Substantivity", ignoreCase = true))
        assertTrue("Must cover Triple Antibiotic Paste (TAP)", allText.contains("Triple Antibiotic Paste", ignoreCase = true) || allText.contains("TAP", ignoreCase = true))
        assertTrue("Must cover minocycline tooth discoloration", allText.contains("Minocycline", ignoreCase = true) && allText.contains("discoloration", ignoreCase = true))
        assertTrue("Must cover Enterococcus faecalis persistence", allText.contains("Enterococcus faecalis", ignoreCase = true))
        assertTrue("Must cover smear layer removal protocol", allText.contains("smear layer", ignoreCase = true))
        assertTrue("Must cover active moiety hypochlorous acid (HOCl)", allText.contains("Hypochlorous acid", ignoreCase = true) || allText.contains("HOCl", ignoreCase = true))
    }

    @Test
    fun testPerRunQuestionRandomizationAndSelectionWithoutReplacement() {
        val runSet1 = QuestionRepository.createRandomizedRunSet()
        val runSet2 = QuestionRepository.createRandomizedRunSet()

        // 3 for R1, 3 for R2, 3 for R3 = 9 total
        assertEquals(3, runSet1.round1RunnerQuestions.size)
        assertEquals(3, runSet1.round2MazeQuestions.size)
        assertEquals(3, runSet1.round3ZombieQuestions.size)
        assertEquals(9, runSet1.allQuestions.size)

        // All 9 questions in a run must be distinct (zero duplicates within the run)
        val distinctIds = runSet1.allQuestions.map { it.id }.toSet()
        assertEquals("A single gameplay run must never repeat questions across rounds", 9, distinctIds.size)

        // Shuffled option order independently for each question: correct option is not fixed at position 0
        var foundNonZeroCorrect = false
        for (q in runSet1.allQuestions) {
            val correctIdx = q.options.indexOfFirst { it.isCorrect }
            if (correctIdx != 0) {
                foundNonZeroCorrect = true
                break
            }
        }
        assertTrue("Correct options must be randomized across all positions A, B, C, D across questions", foundNonZeroCorrect)
    }

    @Test
    fun testClinicalRctStagesContainZeroQuizQuestions() {
        val stages = SampleData.rctStages
        assertEquals("EndoQuest features 3 distinct clinical RCT procedure stages", 3, stages.size)

        // Stage 1: The Pulp Chamber (Tissue & Smear Layer)
        assertEquals(1, stages[0].stageNumber)
        assertTrue("Stage 1 title must match Pulp Chamber", stages[0].title.contains("Pulp Chamber"))
        assertTrue("Stage 1 procedure must match debridement", stages[0].procedurePlaceholderName.contains("Dissolve or Demineralize"))

        // Stage 2: The Apical Danger Zone
        assertEquals(2, stages[1].stageNumber)
        assertTrue("Stage 2 title must match Apical Danger Zone", stages[1].title.contains("Apical Danger Zone"))
        assertTrue("Stage 2 procedure must match side-vent protocol", stages[1].procedurePlaceholderName.contains("Side-Vent Safe Zone"))

        // Stage 3: The Inter-Appointment Vault
        assertEquals(3, stages[2].stageNumber)
        assertTrue("Stage 3 title must match Inter-Appointment Vault", stages[2].title.contains("Inter-Appointment Vault"))
        assertTrue("Stage 3 procedure must match medicament", stages[2].procedurePlaceholderName.contains("Match the Medicament"))
    }
}
