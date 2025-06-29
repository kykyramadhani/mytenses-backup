package com.app.mytenses.model

data class UserData(
    val user_id: Int?,
    val name: String?,
    val email: String?,
    val username: String?,
    val bio: String?,
    val completed_lessons: List<CompletedLesson>?,
    val lessons: Map<String, LessonProgress>?,
    val quiz_scores: Map<String, QuizScore>?,
    val fcm_token: String?
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

data class ChangePasswordRequest(
    val email: String,
    val new_password: String
)

data class LoginResponse(
    val message: String,
    val user: UserData
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class RegisterResponse(
    val message: String,
    val user: UserData?
)

