package com.app.mytenses.model

data class UserData(
    val user_id: Int?,
    val name: String?,
    val email: String?,
    val lessons: Map<String, LessonProgress>?,
    val quiz_scores: Map<String, QuizScore>?
)

data class LessonProgress(
    val progress: Int,
    val status: String
)

data class QuizScore(
    val score_id: String,
    val user_id: String,
    val quiz_id: String,
    val score: Int,
    val date_taken: String
)