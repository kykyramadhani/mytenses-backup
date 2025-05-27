package com.app.mytenses.Activity

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.NoConnectionError
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.app.mytenses.R
import org.json.JSONObject

class UbahKataSandiActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnSave: Button
    private lateinit var backButton: ImageButton
    private lateinit var progressBar: ProgressBar
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_reset_pw)
        } catch (e: Exception) {
            Toast.makeText(this, "Error memuat layout: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Inisialisasi view sesuai XML
        try {
            etEmail = findViewById(R.id.emailPw)
            etNewPassword = findViewById(R.id.NewPw)
            etConfirmPassword = findViewById(R.id.etConfirmPassword)
            btnSave = findViewById(R.id.btnSimpanPw)
            backButton = findViewById(R.id.btnBack)
            progressBar = findViewById(R.id.progressBar)
        } catch (e: Exception) {
            Toast.makeText(this, "Error inisialisasi tampilan: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Tombol kembali
        backButton.setOnClickListener {
            finish() // Kembali ke LoginActivity
        }

        // Toggle password baru
        etNewPassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                etNewPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                etNewPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            etNewPassword.setSelection(etNewPassword.text.length)
        }

        // Toggle konfirmasi password
        etConfirmPassword.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            if (isConfirmPasswordVisible) {
                etConfirmPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                etConfirmPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            etConfirmPassword.setSelection(etConfirmPassword.text.length)
        }

        // Validasi dan Simpan
        btnSave.setOnClickListener {
            try {
                val email = etEmail.text.toString().trim()
                val newPassword = etNewPassword.text.toString().trim()
                val confirmPassword = etConfirmPassword.text.toString().trim()

                if (email.isEmpty()) {
                    etEmail.error = "Email harus diisi"
                    return@setOnClickListener
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    etEmail.error = "Format email tidak valid"
                    return@setOnClickListener
                }

                if (newPassword.isEmpty()) {
                    etNewPassword.error = "Kata sandi baru harus diisi"
                    return@setOnClickListener
                }

                if (confirmPassword.isEmpty()) {
                    etConfirmPassword.error = "Konfirmasi kata sandi harus diisi"
                    return@setOnClickListener
                }

                if (newPassword.length < 6) {
                    Toast.makeText(this, "Kata sandi harus minimal 6", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (newPassword != confirmPassword) {
                    Toast.makeText(this, "Konfirmasi kata sandi tidak cocok", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (!isNetworkAvailable()) {
                    Toast.makeText(this, "Tidak ada koneksi internet", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                progressBar.visibility = ProgressBar.VISIBLE
                btnSave.isEnabled = false

                resetPassword(email, newPassword)
            } catch (e: Exception) {
                Toast.makeText(this, "Error saat validasi: ${e.message}", Toast.LENGTH_LONG).show()
                progressBar.visibility = ProgressBar.GONE
                btnSave.isEnabled = true
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        try {
            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            Toast.makeText(this, "Error memeriksa jaringan: ${e.message}", Toast.LENGTH_LONG).show()
            return false
        }
    }

    private fun resetPassword(email: String, newPassword: String) {
        val url = "https://mytenses-api.vercel.app/api/change-password-by-email"

        val jsonBody = JSONObject().apply {
            put("email", email)
            put("new_password", newPassword)
        }

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.PUT, url, jsonBody,
            { response ->
                progressBar.visibility = ProgressBar.GONE
                btnSave.isEnabled = true
                try {
                    Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, PasswordUpdatedActivity::class.java)
                    startActivity(intent)
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this, "Gagal memproses respons: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                progressBar.visibility = ProgressBar.GONE
                btnSave.isEnabled = true
                val errorMessage = when {
                    error is NoConnectionError -> "Tidak ada koneksi internet"
                    error.networkResponse?.statusCode == 400 -> {
                        try {
                            val errorObj = JSONObject(String(error.networkResponse.data))
                            errorObj.getString("error")
                        } catch (e: Exception) {
                            "Permintaan tidak valid"
                        }
                    }
                    error.networkResponse?.statusCode == 404 -> {
                        try {
                            val errorObj = JSONObject(String(error.networkResponse.data))
                            errorObj.getString("error")
                        } catch (e: Exception) {
                            "Email tidak ditemukan"
                        }
                    }
                    error.networkResponse?.statusCode == 500 -> {
                        try {
                            val errorObj = JSONObject(String(error.networkResponse.data))
                            errorObj.getString("error")
                        } catch (e: Exception) {
                            "Terjadi kesalahan server"
                        }
                    }
                    else -> "Gagal memperbarui kata sandi: ${error.message}"
                }
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            }
        )

        Volley.newRequestQueue(this).add(jsonObjectRequest)
    }
}