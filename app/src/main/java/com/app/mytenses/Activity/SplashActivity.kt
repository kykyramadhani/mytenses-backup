package com.app.mytenses.Activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.app.mytenses.MainActivity
import com.app.mytenses.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Timer 1 detik untuk splash screen
        Handler(Looper.getMainLooper()).postDelayed({
            // Cek status login
            val sharedPreferences = getSharedPreferences("MyTensesPrefs", MODE_PRIVATE)
            val userId = sharedPreferences.getInt("user_id", -1) // -1 sebagai default jika belum login

            val intent = if (userId != -1) {
                // Pengguna sudah login, arahkan ke MainActivity
                Intent(this, MainActivity::class.java)
            } else {
                // Pengguna belum login, arahkan ke OnBoarding1Activity
                Intent(this, OnBoarding1Activity::class.java)
            }

            startActivity(intent)
            finish() // Tutup SplashActivity agar tidak kembali ke sini
        }, 1000) // Delay 1000ms (1 detik)
    }
}