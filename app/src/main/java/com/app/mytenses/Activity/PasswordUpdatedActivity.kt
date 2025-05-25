package com.app.mytenses.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class PasswordUpdatedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_updated)

        val btnSelesai: Button = findViewById(R.id.btnSelesai)

        btnSelesai.setOnClickListener {
            // Aksi ketika tombol selesai ditekan
            finish() // atau arahkan ke halaman login misalnya
            // startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
