package com.app.mytenses.Activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.app.mytenses.R
import com.app.mytenses.model.RegisterRequest
import com.app.mytenses.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class SignUpActivity : AppCompatActivity() {
    private val TAG = "SignUpActivity"

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val etFullName = findViewById<EditText>(R.id.etFullName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val prevBtn = findViewById<ImageButton>(R.id.btnPrev)
        val alreadyHaveAccount = getString(R.string.already_have_account)
        val loginText = getString(R.string.login_here)
        val colorPrimary = String.format("#%06X", (0xFFFFFF and ContextCompat.getColor(this, R.color.blue)))
        val htmlText = alreadyHaveAccount.replace(
            loginText,
            "<font color='$colorPrimary'><u>$loginText</u></font>"
        )
        tvLogin.text = Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY)

        prevBtn.setOnClickListener {
            val intent = Intent(this, OnBoarding5Activity::class.java)
            startActivity(intent)
        }
        btnRegister.setOnClickListener {
            val fullName = etFullName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Harap isi semua kolom", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password != confirmPassword) {
                Toast.makeText(this, "Kata sandi tidak cocok", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Format email tidak valid"
                return@setOnClickListener
            }
            if (password.length < 6) {
                etPassword.error = "Kata sandi harus minimal 6 karakter"
                return@setOnClickListener
            }

            if (!isNetworkAvailable()) {
                Toast.makeText(this, "Tidak ada koneksi internet", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            btnRegister.isEnabled = false

            registerUser(fullName, email, password, progressBar, btnRegister)
        }

        tvLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun registerUser(
        name: String,
        email: String,
        password: String,
        progressBar: ProgressBar,
        btnRegister: Button
    ) {
        lifecycleScope.launch {
            try {
                val request = RegisterRequest(name, email, password)

                progressBar.visibility = View.VISIBLE
                btnRegister.isEnabled = false

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.register(request)
                }

                progressBar.visibility = View.GONE
                btnRegister.isEnabled = true

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    Log.d(TAG, "Register response: $responseBody")

                    if (responseBody?.message == "User registered successfully" && responseBody.user?.email == email) {
                        Toast.makeText(this@SignUpActivity, "Pendaftaran berhasil", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@SignUpActivity, LoginActivity::class.java))
                        finish()
                    } else {
                        Log.e(TAG, "Invalid user data or unexpected message: $responseBody")
                        Toast.makeText(this@SignUpActivity, "Pendaftaran gagal: ${responseBody?.message}", Toast.LENGTH_LONG).show()
                    }
                } else {
                    val errorMessage = when (response.code()) {
                        400 -> "Data tidak valid atau email sudah terdaftar"
                        500 -> "Terjadi kesalahan server"
                        else -> "Gagal mendaftar: ${response.message()}"
                    }
                    Toast.makeText(this@SignUpActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            } catch (e: IOException) {
                progressBar.visibility = View.GONE
                btnRegister.isEnabled = true
                Log.e(TAG, "Network error: ${e.message}", e)
                Toast.makeText(this@SignUpActivity, "Tidak ada koneksi internet", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                btnRegister.isEnabled = true
                Log.e(TAG, "Unexpected error: ${e.message}", e)
                Toast.makeText(this@SignUpActivity, "Gagal memproses data: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}