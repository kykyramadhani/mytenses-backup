package com.app.mytenses

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.app.mytenses.Activity.LoginActivity

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Atur padding untuk system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Ambil TextView untuk pesan selamat datang
        val welcomeTextView = findViewById<TextView>(R.id.textView2)

        // Ambil data dari SharedPreferences
        val sharedPreferences = getSharedPreferences("MyTensesPrefs", MODE_PRIVATE)
        val fullName = sharedPreferences.getString("name", null)
        val userId = sharedPreferences.getInt("user_id", -1)

        // Log untuk debugging
        Log.d(TAG, "SharedPreferences - user_id: $userId, name: $fullName")

        // Periksa apakah pengguna sudah login
        if (userId == -1 || fullName.isNullOrEmpty()) {
            // Pengguna belum login, arahkan ke LoginActivity
            Log.w(TAG, "No user logged in, redirecting to LoginActivity")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Ambil nama depan
        val firstName = fullName.split(" ").firstOrNull() ?: "Pengguna"
        val welcomeText = getString(R.string.welcome_message, firstName)

        // Buat SpannableString untuk mewarnai nama
        val spannable = SpannableString(welcomeText)
        val nameStartIndex = welcomeText.indexOf(firstName)
        val nameEndIndex = nameStartIndex + firstName.length

        // Terapkan warna biru pada nama
        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this, R.color.blue)),
            nameStartIndex,
            nameEndIndex,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Terapkan ke TextView
        welcomeTextView.text = spannable
    }
}