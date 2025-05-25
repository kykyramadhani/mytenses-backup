package com.app.mytenses.Activity

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.NoConnectionError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.app.mytenses.MainActivity
import com.app.mytenses.R
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword) // Tambahkan TextView
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val previousBtn = findViewById<ImageButton>(R.id.btnBack)

        previousBtn.setOnClickListener {
            val intent = Intent(this, OnBoarding5Activity::class.java)
            startActivity(intent)
        }

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

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Format email tidak valid"
                return@setOnClickListener
            }

            if (!isNetworkAvailable()) {
                Toast.makeText(this, "Tidak ada koneksi internet", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            btnLogin.isEnabled = false

            loginUser(email, password, progressBar, btnLogin)
        }

        tvLogin.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        // Navigasi ke ResetPasswordActivity
        tvForgotPassword.setOnClickListener {
            val intent = Intent(this, ResetPasswordActivity::class.java)
            startActivity(intent)
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun loginUser(email: String, password: String, progressBar: ProgressBar, btnLogin: Button) {
        val url = "https://mytenses-api.vercel.app/api/login"

        val jsonBody = JSONObject().apply {
            put("email", email)
            put("password", password)
        }

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, jsonBody,
            Response.Listener { response ->
                try {
                    progressBar.visibility = View.GONE
                    btnLogin.isEnabled = true

                    val user = response.getJSONObject("user")
                    val name = user.getString("name")
                    val userId = user.getInt("user_id")
                    val username = user.getString("username")

                    val sharedPreferences = getSharedPreferences("MyTensesPrefs", MODE_PRIVATE)
                    sharedPreferences.edit()
                        .putInt("user_id", userId)
                        .putString("username", username)
                        .putString("name", name)
                        .apply()

                    Toast.makeText(this, "Login berhasil! Selamat datang, $name", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } catch (e: Exception) {
                    progressBar.visibility = View.GONE
                    btnLogin.isEnabled = true
                    Toast.makeText(this, "Gagal memproses data: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            Response.ErrorListener { error ->
                progressBar.visibility = View.GONE
                btnLogin.isEnabled = true
                val errorMessage = when {
                    error is NoConnectionError -> "Tidak ada koneksi internet"
                    error.networkResponse?.statusCode == 401 -> {
                        try {
                            val errorObj = JSONObject(String(error.networkResponse.data))
                            errorObj.getString("error")
                        } catch (e: Exception) {
                            "Email atau password salah"
                        }
                    }
                    error.networkResponse?.statusCode == 500 -> "Terjadi kesalahan server"
                    else -> "Gagal login: ${error.message}"
                }
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            }
        )

        Volley.newRequestQueue(this).add(jsonObjectRequest)
    }
}