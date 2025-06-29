package com.app.mytenses.Activity

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.app.mytenses.R
import com.app.mytenses.network.RetrofitClient
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    private var currentFcmToken: String? = null
    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences("MyTensesPrefs", MODE_PRIVATE)
        val userId = sharedPreferences.getInt("user_id", -1)
        if (userId != -1) {
            Log.d(TAG, "User already logged in, navigating to MainActivity")
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        // Save FCM token
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                currentFcmToken = task.result
                Log.d(TAG, "FCM token retrieved: $currentFcmToken")
            } else {
                currentFcmToken = null
                Log.w(TAG, "Failed to retrieve FCM token: ${task.exception?.message}")
            }
        }

        setContentView(R.layout.activity_login)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val previousBtn = findViewById<ImageButton>(R.id.btnBack)
        val changePassword = findViewById<TextView>(R.id.changePassword)

        previousBtn.setOnClickListener {
            val intent = Intent(this, OnBoarding5Activity::class.java)
            startActivity(intent)
        }

        changePassword.setOnClickListener {
            val intent = Intent(this, UbahKataSandiActivity::class.java)
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
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun loginUser(email: String, password: String, progressBar: ProgressBar, btnLogin: Button) {
        lifecycleScope.launch {
            try {
                val loginBody = mapOf(
                    "email" to email,
                    "password" to password,
                    "fcm_token" to (currentFcmToken ?: "")
                )

                Log.d(TAG, "Sending login request with body: $loginBody")

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.login(loginBody)
                }

                progressBar.visibility = View.GONE
                btnLogin.isEnabled = true

                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    val user = loginResponse?.user

                    if (user != null && user.user_id != null && user.user_id != -1) {
                        val sharedPreferences = getSharedPreferences("MyTensesPrefs", MODE_PRIVATE)
                        sharedPreferences.edit()
                            .putInt("user_id", user.user_id)
                            .putString("username", user.username)
                            .putString("name", user.name)
                            .putString("email", user.email)
                            .putString("bio", user.bio)
                            .putString("fcm_token", currentFcmToken)
                            .apply()

                        Log.d(TAG, "Login successful, user: $user, SharedPreferences: ${sharedPreferences.all}")
                        Toast.makeText(this@LoginActivity, "Login berhasil! Selamat datang, ${user.name}", Toast.LENGTH_SHORT).show()

                        Log.d(TAG, "Navigating to MainActivity")
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Log.w(TAG, "Invalid user data: $loginResponse")
                        Toast.makeText(this@LoginActivity, "Gagal login: Data pengguna tidak valid", Toast.LENGTH_LONG).show()
                    }
                } else {
                    val errorMessage = when (response.code()) {
                        401 -> {
                            try {
                                val errorBody = response.errorBody()?.string()
                                errorBody?.let { JSONObject(it).getString("error") } ?: "Email atau password salah"
                            } catch (e: Exception) {
                                "Email atau password salah"
                            }
                        }
                        500 -> "Terjadi kesalahan server"
                        else -> "Gagal login: ${response.message()}"
                    }
                    Log.w(TAG, "Login failed with code ${response.code()}: $errorMessage")
                    Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            } catch (e: IOException) {
                progressBar.visibility = View.GONE
                btnLogin.isEnabled = true
                Log.e(TAG, "Network error: ${e.message}", e)
                Toast.makeText(this@LoginActivity, "Tidak ada koneksi internet", Toast.LENGTH_LONG).show()
            } catch (e: HttpException) {
                progressBar.visibility = View.GONE
                btnLogin.isEnabled = true
                Log.e(TAG, "HTTP error: ${e.message()}", e)
                Toast.makeText(this@LoginActivity, "Gagal login: ${e.message()}", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                btnLogin.isEnabled = true
                Log.e(TAG, "Unexpected error: ${e.message}", e)
                Toast.makeText(this@LoginActivity, "Gagal memproses data: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}