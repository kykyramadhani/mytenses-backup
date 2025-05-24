package com.app.mytenses.Activity


import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.app.mytenses.MainActivity
import com.app.mytenses.R

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login) // pastikan nama file XML-nya 'activity_login.xml'

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnRegister)
        val tvRegister = findViewById<TextView>(R.id.tvLogin)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty()) {
                etEmail.error = "Email harus diisi"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                etPassword.error = "Password harus diisi"
                return@setOnClickListener
            }

            // Contoh validasi login sederhana (email dan password statis)
            if (email == "admin@example.com" && password == "123456") {
                Toast.makeText(this, "Login berhasil!", Toast.LENGTH_SHORT).show()
                // Navigasi ke halaman berikutnya
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Email atau password salah", Toast.LENGTH_SHORT).show()
            }
        }

        tvRegister.setOnClickListener {
            // Arahkan ke halaman register
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }
}
