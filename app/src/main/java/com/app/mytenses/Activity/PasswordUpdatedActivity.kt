package com.app.mytenses.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.app.mytenses.R

class PasswordUpdatedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_updated)

        val btnSelesai: Button = findViewById(R.id.btnResetPw) // Diperbaiki dari btnSelesai ke btnResetPw

        btnSelesai.setOnClickListener {
            // Arahkan ke LoginActivity setelah kata sandi diperbarui
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Tutup aktivitas ini agar tidak kembali ke sini
        }
    }
}