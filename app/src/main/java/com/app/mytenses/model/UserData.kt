package com.app.mytenses.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "users")
data class UserData(
    @PrimaryKey val user_id: Int?,
    val name: String?,
    val email: String?,
    val username: String?,
    val bio: String?,
    val completed_lessons: List<CompletedLesson>?,
    val lessons: Map<String, LessonProgress>?,
    val quiz_scores: Map<String, QuizScore>?
)

@Entity(tableName = "completed_lessons")
data class CompletedLesson(
    @PrimaryKey val lesson_id: String,
    val title: String,
    val user_id: Int?
)

data class LessonProgress(
    val progress: Int,
    val status: String
)

@Entity(tableName = "quiz_scores")
data class QuizScore(
    @PrimaryKey val score_id: String,
    val user_id: String,
    val quiz_id: String,
    val score: Int,
    val date_taken: String
)

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromCompletedLessons(value: List<CompletedLesson>?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toCompletedLessons(value: String?): List<CompletedLesson>? {
        return gson.fromJson(value, object : TypeToken<List<CompletedLesson>>() {}.type)
    }

    @TypeConverter
    fun fromLessonProgressMap(value: Map<String, LessonProgress>?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toLessonProgressMap(value: String?): Map<String, LessonProgress>? {
        return gson.fromJson(value, object : TypeToken<Map<String, LessonProgress>>() {}.type)
    }

    @TypeConverter
    fun fromQuizScoreMap(value: Map<String, QuizScore>?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toQuizScoreMap(value: String?): Map<String, QuizScore>? {
        return gson.fromJson(value, object : TypeToken<Map<String, QuizScore>>() {}.type)
    }
}