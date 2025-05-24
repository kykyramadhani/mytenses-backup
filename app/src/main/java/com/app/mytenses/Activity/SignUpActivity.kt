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
import com.android.volley.DefaultRetryPolicy
import com.android.volley.NoConnectionError
import com.android.volley.Response
import com.android.volley.TimeoutError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.app.mytenses.R
import org.json.JSONObject

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

    private fun registerUser(name: String, email: String, password: String, progressBar: ProgressBar, btnRegister: Button) {
        val url = "https://mytenses-api.vercel.app/api/register"
        Log.d(TAG, "Sending register request to $url with body: { name: $name, email: $email, password: [HIDDEN] }")

        val jsonBody = JSONObject().apply {
            put("name", name)
            put("email", email)
            put("password", password)
        }

        val jsonObjectRequest = object : JsonObjectRequest(
            Method.POST, url, jsonBody,
            Response.Listener { response ->
                progressBar.visibility = View.GONE
                btnRegister.isEnabled = true

                Log.d(TAG, "Register response: $response")

                try {
                    val message = response.optString("message", "")
                    if (message == "User registered successfully") {
                        if (response.has("user")) {
                            val user = response.getJSONObject("user")
                            val userId = user.optInt("user_id", -1)
                            val userEmail = user.optString("email", "")
                            if (userId != -1 && userEmail == email) {
                                Toast.makeText(this, "Pendaftaran berhasil", Toast.LENGTH_SHORT).show()
                                // Redirect ke LoginActivity
                                val intent = Intent(this, LoginActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                Log.e(TAG, "Invalid user data in response: $response")
                                Toast.makeText(this, "Pendaftaran gagal: Data pengguna tidak valid", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Log.e(TAG, "No user data in response: $response")
                            Toast.makeText(this, "Pendaftaran gagal: Tidak ada data pengguna", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Log.e(TAG, "Unexpected message in response: $message")
                        Toast.makeText(this, "Pendaftaran gagal: $message", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing response: ${e.message}", e)
                    Toast.makeText(this, "Gagal memproses data: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            Response.ErrorListener { error ->
                progressBar.visibility = View.GONE
                btnRegister.isEnabled = true

                Log.e(TAG, "Register error: ${error.message}", error)

                val errorMessage = when {
                    error is NoConnectionError -> "Tidak ada koneksi internet"
                    error is TimeoutError -> "Permintaan ke server timeout"
                    error.networkResponse != null -> {
                        try {
                            val errorObj = JSONObject(String(error.networkResponse.data))
                            val errorMsg = errorObj.optString("error", "Kesalahan tidak diketahui")
                            val details = errorObj.optString("details", "")
                            "$errorMsg${if (details.isNotEmpty()) ": $details" else ""}"
                        } catch (e: Exception) {
                            when (error.networkResponse.statusCode) {
                                400 -> "Data tidak valid atau email sudah terdaftar"
                                500 -> "Terjadi kesalahan server"
                                else -> "Gagal mendaftar: ${error.message}"
                            }
                        }
                    }
                    else -> "Gagal mendaftar: ${error.message}"
                }
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getHeaders(): Map<String, String> {
                return mapOf("Content-Type" to "application/json")
            }
        }

        jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
            10000, // Timeout 10 detik
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        Volley.newRequestQueue(this).add(jsonObjectRequest)
    }
}