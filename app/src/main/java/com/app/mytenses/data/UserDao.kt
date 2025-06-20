package com.app.mytenses.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.app.mytenses.model.CompletedLesson
import com.app.mytenses.model.QuizScore
import com.app.mytenses.model.UserData

@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: UserData)

    @Update
    suspend fun updateUser(user: UserData)

    @Query("SELECT * FROM users WHERE user_id = :userId")
    suspend fun getUserById(userId: Int): UserData?

    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): UserData?

    @Insert
    suspend fun insertCompletedLesson(lesson: CompletedLesson)

    @Query("SELECT * FROM completed_lessons WHERE user_id = :userId")
    suspend fun getCompletedLessonsByUser(userId: Int): List<CompletedLesson>

    @Insert
    suspend fun insertQuizScore(score: QuizScore)

    @Query("SELECT * FROM quiz_scores WHERE user_id = :userId")
    suspend fun getQuizScoresByUser(userId: String): List<QuizScore>

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()

    @Query("DELETE FROM completed_lessons")
    suspend fun deleteAllCompletedLessons()

    @Query("DELETE FROM quiz_scores")
    suspend fun deleteAllQuizScores()
}