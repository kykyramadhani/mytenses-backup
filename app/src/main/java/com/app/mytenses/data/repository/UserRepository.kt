package com.app.mytenses.data.repository

import android.content.Context
import android.util.Log
import com.app.mytenses.data.dao.UserDao
import com.app.mytenses.data.entity.LessonProgressEntity
import com.app.mytenses.data.entity.QuizScoreEntity
import com.app.mytenses.data.entity.UserEntity
import com.app.mytenses.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(
    private val apiService: ApiService,
    private val context: Context,
    private val userDao: UserDao
) {
    suspend fun syncUserData(username: String) {
        withContext(Dispatchers.IO) {
            try {
                val userResponse = apiService.getUserData(username)
                if (userResponse.isSuccessful) {
                    val user = userResponse.body()
                    if (user != null) {
                        val userEntity = UserEntity(
                            username = user.username ?: username,
                            name = user.name ?: "",
                            email = user.email ?: "",
                            bio = user.bio ?: "",
                            completedLessons = user.completed_lessons?.map { it.lesson_id } ?: emptyList()
                        )
                        val lessonProgressResponse = apiService.getLessonProgress(username)
                        val progressList = if (lessonProgressResponse.isSuccessful) {
                            lessonProgressResponse.body()?.lesson_progress?.map { progress ->
                                LessonProgressEntity(
                                    userId = username,
                                    lessonId = progress.lesson_id,
                                    status = progress.status,
                                    progress = progress.progress
                                )
                            } ?: emptyList()
                        } else {
                            emptyList()
                        }
                        val quizScores = user.quiz_scores?.map { (scoreId, quizScore) ->
                            QuizScoreEntity(
                                scoreId = quizScore.score_id,
                                userId = quizScore.user_id,
                                quizId = quizScore.quiz_id,
                                score = quizScore.score,
                                dateTaken = quizScore.date_taken
                            )
                        } ?: emptyList()
                        userDao.insertUserData(userEntity, progressList, quizScores)
                    } else {
                        Log.e("UserRepository", "User data is null")
                    }
                } else {
                    Log.e("UserRepository", "Failed to fetch user data: ${userResponse.message()}, Code: ${userResponse.code()}")
                }
            } catch (e: Exception) {
                Log.e("UserRepository", "Error syncing user data: ${e.message}")
            }
        }
    }

    suspend fun getUserByUsername(username: String): UserEntity? {
        return withContext(Dispatchers.IO) {
            userDao.getUserByUsername(username)
        }
    }

    suspend fun getLessonProgressByUser(userId: String): List<LessonProgressEntity> {
        return withContext(Dispatchers.IO) {
            userDao.getLessonProgressByUser(userId)
        }
    }

    suspend fun getQuizScoresByUser(userId: String): List<QuizScoreEntity> {
        return withContext(Dispatchers.IO) {
            userDao.getQuizScoresByUser(userId)
        }
    }
}