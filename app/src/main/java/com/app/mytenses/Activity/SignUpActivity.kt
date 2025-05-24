package com.app.mytenses.Activity

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.app.mytenses.R

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Inisialisasi komponen
        val etFullName = findViewById<EditText>(R.id.etFullName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)

        // Atur teks "Sudah punya akun? Masuk Disini" dengan HTML
        val alreadyHaveAccount = getString(R.string.already_have_account)
        val loginText = getString(R.string.login_here)
        // Ambil warna colorPrimary dan konversi ke heksadesimal
        val colorPrimary = String.format("#%06X", (0xFFFFFF and ContextCompat.getColor(this, R.color.blue)))
        // Format teks dengan HTML: hitam untuk "Sudah punya akun?", colorPrimary dan garis bawah untuk "Masuk Disini"
        val htmlText = alreadyHaveAccount.replace(
            loginText,
            "<font color='$colorPrimary'><u>$loginText</u></font>"
        )
        // Terapkan teks HTML ke TextView
        tvLogin.text = Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY)

        // Aksi tombol Daftar
        btnRegister.setOnClickListener {
            val fullName = etFullName.text.toString()
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Harap isi semua kolom", Toast.LENGTH_SHORT).show()
            } else if (password != confirmPassword) {
                Toast.makeText(this, "Kata sandi tidak cocok", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Pendaftaran berhasil", Toast.LENGTH_SHORT).show()
            }
        }

        // Aksi teks Masuk
        tvLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}