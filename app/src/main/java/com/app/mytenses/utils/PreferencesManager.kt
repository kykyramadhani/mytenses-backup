package com.app.mytenses.utils

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val sharedPref: SharedPreferences =
        context.getSharedPreferences("MyTensesPrefs", Context.MODE_PRIVATE)

    fun saveUserSession(userId: Int, username: String) {
        with(sharedPref.edit()) {
            putInt("user_id", userId)
            putString("username", username)
            apply()
        }
    }

    fun getUserId(): Int = sharedPref.getInt("user_id", -1)

    fun getUsername(): String? = sharedPref.getString("username", null)

    fun clearSession() {
        with(sharedPref.edit()) {
            clear()
            apply()
        }
    }
}