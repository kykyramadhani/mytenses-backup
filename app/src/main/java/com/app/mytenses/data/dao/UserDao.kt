package com.app.mytenses.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.app.mytenses.data.entity.LessonProgressEntity
import com.app.mytenses.data.entity.QuizScoreEntity
import com.app.mytenses.data.entity.UserEntity

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessonProgress(progress: LessonProgressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizScore(score: QuizScoreEntity)

    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM lesson_progress WHERE userId = :userId")
    suspend fun getLessonProgressByUser(userId: String): List<LessonProgressEntity>

    @Query("SELECT * FROM quiz_scores WHERE userId = :userId")
    suspend fun getQuizScoresByUser(userId: String): List<QuizScoreEntity>

    @Transaction
    suspend fun insertUserData(
        user: UserEntity,
        lessonProgress: List<LessonProgressEntity>,
        quizScores: List<QuizScoreEntity>
    ) {
        insertUser(user)
        lessonProgress.forEach { insertLessonProgress(it) }
        quizScores.forEach { insertQuizScore(it) }
    }
}