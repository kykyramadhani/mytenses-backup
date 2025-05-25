package com.app.mytenses

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.app.mytenses.Activity.ClassFragment
import com.app.mytenses.Activity.HomeFragment
import com.app.mytenses.Activity.NotificationFragment
import com.app.mytenses.Activity.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.fragment.app.Fragment
import com.app.mytenses.Activity.LoginActivity

class MainActivity : AppCompatActivity() {
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

        Log.d(TAG, "SharedPreferences - user_id: $userId, name: $fullName")

        if (userId == -1 || fullName.isNullOrEmpty()) {
            // Pengguna belum login, arahkan ke LoginActivity
            Log.w(TAG, "No user logged in, redirecting to LoginActivity")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.nav_class -> {
                    replaceFragment(ClassFragment())
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

    private val TAG = "MainActivity"
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}