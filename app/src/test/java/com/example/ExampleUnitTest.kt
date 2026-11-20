package com.example

import com.example.data.local.StudyQuestionEntity
import com.example.ui.components.formatCountdownMmSs
import com.example.ui.viewmodel.ChallengeQuiz
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun mathQuiz_validatesCorrectAnswer() {
        val quiz = ChallengeQuiz.MathQuiz("14 + 16", 30)
        assertTrue(quiz.isCorrect("30"))
        assertTrue(quiz.isCorrect(" 30 "))
        assertFalse(quiz.isCorrect("29"))
    }

    @Test
    fun bookQuiz_validatesCaseInsensitiveAnswer() {
        val q = StudyQuestionEntity(
            question = "পানির রাসায়নিক সংকেত কী?",
            answer = "H2O",
            subjectNote = "বিজ্ঞান"
        )
        val quiz = ChallengeQuiz.BookQuiz(q)
        assertTrue(quiz.isCorrect("h2o"))
        assertFalse(quiz.isCorrect("CO2"))
    }

    @Test
    fun countdownFormatting_padsMmSs() {
        val now = 1_000_000L
        val target = now + 295_000L // 4m 55s
        assertEquals("04:55", formatCountdownMmSs(target, now))
    }
}
