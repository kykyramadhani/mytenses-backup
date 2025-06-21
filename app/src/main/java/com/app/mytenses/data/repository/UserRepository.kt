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
                if (username.isEmpty()) {
                    Log.e("UserRepository", "Username is empty, cannot sync data")
                    return@withContext
                }
                Log.d("UserRepository", "Starting syncUserData for username: $username")
                val userResponse = apiService.getUserData(username)
                Log.d("UserRepository", "API Response Code: ${userResponse.code()}")
                if (userResponse.isSuccessful) {
                    val user = userResponse.body()
                    if (user != null) {
                        // Simpan UserEntity
                        val userEntity = UserEntity(
                            username = user.username ?: username,
                            name = user.name ?: "",
                            email = user.email ?: "",
                            bio = user.bio ?: "",
                            completedLessons = user.completed_lessons?.map { it.lesson_id } ?: emptyList()
                        )
                        userDao.insertUser(userEntity)
                        Log.d("UserRepository", "User inserted into database: $userEntity")

                        // Simpan LessonProgressEntity
                        val lessonProgressResponse = apiService.getLessonProgress(username)
                        if (lessonProgressResponse.isSuccessful) {
                            val progressList = lessonProgressResponse.body()?.lesson_progress ?: emptyList()
                            progressList.forEach { progress ->
                                val lessonProgress = LessonProgressEntity(
                                    userId = username,
                                    lessonId = progress.lesson_id,
                                    status = progress.status,
                                    progress = progress.progress
                                )
                                userDao.insertLessonProgress(lessonProgress)
                                Log.d("UserRepository", "Lesson progress inserted: $lessonProgress")
                            }
                        } else {
                            Log.e("UserRepository", "Failed to fetch lesson progress: ${lessonProgressResponse.message()}, Code: ${lessonProgressResponse.code()}")
                        }

                        // Simpan QuizScoreEntity
                        val quizScores = user.quiz_scores ?: emptyMap()
                        quizScores.forEach { (scoreId, quizScore) ->
                            val quizScoreEntity = QuizScoreEntity(
                                scoreId = quizScore.score_id,
                                userId = quizScore.user_id,
                                quizId = quizScore.quiz_id,
                                score = quizScore.score,
                                dateTaken = quizScore.date_taken
                            )
                            userDao.insertQuizScore(quizScoreEntity)
                            Log.d("UserRepository", "Quiz score inserted: $quizScoreEntity")
                        }
                    } else {
                        Log.e("UserRepository", "User data is null")
                    }
                } else {
                    Log.e("UserRepository", "Failed to fetch user data: ${userResponse.message()}, Code: ${userResponse.code()}")
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) {
                    Log.d("UserRepository", "Sync user data cancelled")
                    throw e // Re-throw untuk memastikan pembatalan ditangani oleh caller
                }
                Log.e("UserRepository", "Error syncing user data: ${e.message}, StackTrace: ${e.stackTraceToString()}")
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