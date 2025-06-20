package com.app.mytenses.utils

import android.content.Context
import com.app.mytenses.data.AppDatabase
import com.app.mytenses.model.UserData

class DataManager(private val context: Context) {
    private val prefsManager = PreferencesManager(context)
    private val db = AppDatabase.getDatabase(context)

    // Simpan sesi pengguna di SharedPreferences
    fun saveUserSession(userId: Int, username: String) {
        prefsManager.saveUserSession(userId, username)
    }

    // Simpan data pengguna ke Room
    suspend fun saveUser(user: UserData) {
        db.userDao().insertUser(user)
        user.completed_lessons?.forEach { lesson ->
            db.userDao().insertCompletedLesson(lesson.copy(user_id = user.user_id))
        }
        user.quiz_scores?.values?.forEach { score ->
            db.userDao().insertQuizScore(score)
        }
    }

    // Ambil data pengguna dari Room
    suspend fun getUserData(username: String): UserData? {
        val user = db.userDao().getUserByUsername(username)
        return user?.copy(
            completed_lessons = db.userDao().getCompletedLessonsByUser(user.user_id ?: -1),
            quiz_scores = db.userDao().getQuizScoresByUser(user.user_id?.toString() ?: "")
                .associateBy { it.score_id }
        )
    }

    // Sinkronkan data dari Firebase ke Room
    suspend fun syncFromFirebase(userData: UserData) {
        db.userDao().insertUser(userData)
        userData.completed_lessons?.forEach { lesson ->
            db.userDao().insertCompletedLesson(lesson.copy(user_id = userData.user_id))
        }
        userData.quiz_scores?.values?.forEach { score ->
            db.userDao().insertQuizScore(score)
        }
    }

    // Ambil user_id dari SharedPreferences
    fun getCurrentUserId(): Int = prefsManager.getUserId()

    // Bersihkan data lokal
    suspend fun clearLocalData() {
        prefsManager.clearSession()
        db.userDao().deleteAllUsers()
        db.userDao().deleteAllCompletedLessons()
        db.userDao().deleteAllQuizScores()
    }
}