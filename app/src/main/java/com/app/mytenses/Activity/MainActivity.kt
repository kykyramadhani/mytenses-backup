package com.app.mytenses.Activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.app.mytenses.Fragment.CourseFragment
import com.app.mytenses.Fragment.HomeFragment
import com.app.mytenses.Fragment.NotificationFragment
import com.app.mytenses.Fragment.ProfileFragment
import com.app.mytenses.data.database.AppDatabase
import com.app.mytenses.data.repository.UserRepository
import com.app.mytenses.network.RetrofitClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.app.mytenses.R
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private lateinit var userRepository: UserRepository
    private val appDatabase: AppDatabase by lazy {
        AppDatabase.getDatabase(this) // Inisialisasi database saat pertama kali diakses
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val sharedPreferences = getSharedPreferences("MyTensesPrefs", MODE_PRIVATE)
        val fullName = sharedPreferences.getString("name", null)
        val userId = sharedPreferences.getInt("user_id", -1)
        val username = if (userId != -1) "user_$userId" else ""
        Log.d(TAG, "SharedPreferences - user_id: $userId, name: $fullName, username: $username")


        if (userId == -1 || fullName.isNullOrEmpty()) {
            Log.w(TAG, "No user logged in, redirecting to LoginActivity")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Inisialisasi UserRepository dengan database
        userRepository = UserRepository(RetrofitClient.apiService, this, appDatabase.userDao())

        lifecycleScope.launch {
            try {
                userRepository.syncUserData(username)
                Log.d(TAG, "User data synced successfully for userId: $userId")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync user data: ${e.message}")
            }
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.nav_class -> {
                    replaceFragment(CourseFragment())
                    true
                }
                R.id.nav_notification -> {
                    replaceFragment(NotificationFragment())
                    true
                }
                R.id.nav_profile -> {
                    replaceFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
        replaceFragment(HomeFragment())
        bottomNavigationView.selectedItemId = R.id.nav_home
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}