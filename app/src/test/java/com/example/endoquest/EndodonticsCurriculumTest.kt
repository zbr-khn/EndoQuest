package com.example.endoquest

import com.example.endoquest.data.SampleData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests validating the clinical accuracy, educational completeness,
 * and scientific integrity of the 4th-Year BDS Endodontics curriculum database.
 */
class EndodonticsCurriculumTest {

    @Test
    fun testAllCurriculumQuestionsHaveValidContentAndOptions() {
        val questionPool = SampleData.runEndodonticsQuestionPool
        assertTrue("Question pool should contain multiple questions", questionPool.size >= 10)

        for (question in questionPool) {
            assertNotNull("Question ID must not be null", question.id)
            assertTrue("Question ID must not be blank", question.id.isNotBlank())
            assertTrue("Question text must be substantive (${question.id})", question.questionText.length > 15)
            assertTrue("Must have at least 4 MCQ options (${question.id})", question.rawOptions.size >= 4)
            assertTrue("Explanation must provide clinical rationale (${question.id})", question.explanation.length > 20)
            assertEquals("Difficulty must adhere to 4th-Year BDS standards (${question.id})", "4th-Year BDS", question.difficulty)
            assertTrue("Category must be clinically categorized (${question.id})", question.category.isNotBlank())

            // Options must be non-empty and unique
            for (opt in question.rawOptions) {
                assertTrue("Option text must not be blank", opt.isNotBlank())
            }
            assertEquals("All options for a question must be unique", question.rawOptions.toSet().size, question.rawOptions.size)
        }
    }

    @Test
    fun testCorrectAnswerStrictlyExistsInOptions() {
        val questionPool = SampleData.runEndodonticsQuestionPool
        for (question in questionPool) {
            assertTrue(
                "Correct answer '${question.correctAnswerText}' must be present in rawOptions for question ID: ${question.id}",
                question.rawOptions.contains(question.correctAnswerText)
            )
        }
    }

    @Test
    fun testCoreEndodonticPharmacologyAndTechniquesCovered() {
        val questionPool = SampleData.runEndodonticsQuestionPool
        val allText = questionPool.joinToString(separator = " ") { "${it.questionText} ${it.rawOptions.joinToString()} ${it.explanation}" }

        // Must cover primary irrigants and core concepts
        assertTrue("Curriculum must test Sodium hypochlorite", allText.contains("Sodium hypochlorite", ignoreCase = true))
        assertTrue("Curriculum must test Gutta-percha", allText.contains("Gutta-percha", ignoreCase = true))
        assertTrue("Curriculum must test working length & apex locator", allText.contains("Apex locator", ignoreCase = true) || allText.contains("constriction", ignoreCase = true))
        assertTrue("Curriculum must test resistant microorganisms like E. faecalis", allText.contains("Enterococcus faecalis", ignoreCase = true))
        assertTrue("Curriculum must test smear layer dynamics", allText.contains("smear layer", ignoreCase = true))
    }

    @Test
    fun testRctSimulationStagesStructure() {
        val stages = SampleData.rctStages
        assertEquals("EndoQuest must feature 3 distinct clinical RCT stages", 3, stages.size)

        // Stage 1: Pulp Chamber (EDTA vs NaOCl)
        assertTrue("Stage 1 title must match Pulp Chamber", stages[0].title.contains("Pulp Chamber"))
        assertTrue("Stage 1 question must relate to EDTA / smear layer", stages[0].quizQuestion.questionText.contains("EDTA"))

        // Stage 2: Apical Danger Zone (Chemical Safety & PCA)
        assertTrue("Stage 2 title must match Apical Danger Zone", stages[1].title.contains("Apical Danger Zone"))
        assertTrue("Stage 2 question must relate to Chlorhexidine / PCA hazard", stages[1].quizQuestion.questionText.contains("Chlorhexidine"))

        // Stage 3: Inter-Appointment Vault (Ca(OH)2)
        assertTrue("Stage 3 title must match Inter-Appointment Vault", stages[2].title.contains("Inter-Appointment Vault"))
        assertTrue("Stage 3 question must relate to Calcium Hydroxide mechanism", stages[2].quizQuestion.questionText.contains("Calcium Hydroxide"))
    }

    @Test
    fun testRoundQuestionPartitioning() {
        // Round 1 (Runner) questions
        assertEquals(3, SampleData.round1Questions.size)
        // Round 2 (Maze) questions
        assertEquals(3, SampleData.round2MazeQuestions.size)
        // Round 3 (Zombie) questions
        assertEquals(4, SampleData.round3ZombieQuestions.size)
    }
}
