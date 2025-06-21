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
                Log.d("UserRepository", "Starting syncUserData for username: $username")
                val userResponse = apiService.getUserData(username)
                Log.d("UserRepository", "API Response Code: ${userResponse.code()}")
                Log.d("UserRepository", "API Response Message: ${userResponse.message()}")
                Log.d("UserRepository", "API Response Body: ${userResponse.body()}")
                Log.d("UserRepository", "API Response Raw: ${userResponse.raw()}")
                if (userResponse.isSuccessful) {
                    Log.d("UserRepository", "User data fetched successfully")
                    val user = userResponse.body()
                    if (user != null) {
                        Log.d("UserRepository", "User data: $user")
                        val userEntity = UserEntity(
                            username = user.username ?: username,
                            name = user.name ?: "",
                            email = user.email ?: "",
                            bio = user.bio ?: "",
                            completedLessons = user.completed_lessons?.map { it.lesson_id } ?: emptyList()
                        )
                        Log.d("UserRepository", "User entity created: $userEntity")
                        userDao.insertUser(userEntity)
                        Log.d("UserRepository", "User inserted into database: $userEntity")

                        val lessonProgressResponse = apiService.getLessonProgress(username)
                        Log.d("UserRepository", "Lesson Progress Response Code: ${lessonProgressResponse.code()}")
                        if (lessonProgressResponse.isSuccessful) {
                            Log.d("UserRepository", "Lesson progress fetched successfully")
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
                    } else {
                        Log.e("UserRepository", "User data is null")
                    }
                } else {
                    Log.e("UserRepository", "Failed to fetch user data: ${userResponse.message()}, Code: ${userResponse.code()}")
                }
            } catch (e: Exception) {
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