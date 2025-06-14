package com.app.mytenses.model

data class UserData(
    val user_id: Int?,
    val name: String?,
    val email: String?,
    val username: String?,
    val bio: String?,
    val completed_lessons: List<CompletedLesson>?,
    val lessons: Map<String, LessonProgress>?,
    val quiz_scores: Map<String, QuizScore>?
)

data class CompletedLesson(
    val lesson_id: String,
    val title: String
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