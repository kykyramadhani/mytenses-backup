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
import androidx.lifecycle.lifecycleScope
import com.app.mytenses.R
import com.app.mytenses.network.ApiService
import com.app.mytenses.model.ChangePasswordRequest
import com.app.mytenses.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

class UbahKataSandiActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnSave: Button
    private lateinit var backButton: ImageButton
    private lateinit var progressBar: ProgressBar
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false
    private val apiService: ApiService by lazy { RetrofitClient.apiService }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_reset_pw)
        } catch (e: Exception) {
            Toast.makeText(this, "Error memuat layout: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
            return
        }

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
            finish()
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
                    Toast.makeText(this, "Kata sandi harus minimal 6 karakter", Toast.LENGTH_SHORT).show()
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

                lifecycleScope.launch {
                    resetPassword(email, newPassword)
                }
            } catch (e: Exception) {
                Toast.makeText(this@UbahKataSandiActivity, "Error saat validasi: ${e.message}", Toast.LENGTH_LONG).show()
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

    private suspend fun resetPassword(email: String, newPassword: String) {
        try {
            val requestBody = ChangePasswordRequest(email, newPassword)
            val response = withContext(Dispatchers.IO) {
                apiService.changePasswordByEmail(requestBody)
            }
            withContext(Dispatchers.Main) {
                progressBar.visibility = ProgressBar.GONE
                btnSave.isEnabled = true
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val message = responseBody?.get("message") ?: "Kata sandi berhasil diperbarui"
                    Toast.makeText(this@UbahKataSandiActivity, message, Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@UbahKataSandiActivity, PasswordUpdatedActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val errorMessage = when (response.code()) {
                        400 -> {
                            try {
                                val errorBody = response.errorBody()?.string()
                                JSONObject(errorBody ?: "{}").getString("error") ?: "Permintaan tidak valid"
                            } catch (e: Exception) {
                                "Permintaan tidak valid"
                            }
                        }
                        404 -> {
                            try {
                                val errorBody = response.errorBody()?.string()
                                JSONObject(errorBody ?: "{}").getString("error") ?: "Email tidak ditemukan"
                            } catch (e: Exception) {
                                "Email tidak ditemukan"
                            }
                        }
                        500 -> {
                            try {
                                val errorBody = response.errorBody()?.string()
                                JSONObject(errorBody ?: "{}").getString("error") ?: "Terjadi kesalahan server"
                            } catch (e: Exception) {
                                "Terjadi kesalahan server"
                            }
                        }
                        else -> "Gagal memperbarui kata sandi: ${response.message()}"
                    }
                    Toast.makeText(this@UbahKataSandiActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                progressBar.visibility = ProgressBar.GONE
                btnSave.isEnabled = true
                val errorMessage = when (e) {
                    is IOException -> "Tidak ada koneksi internet"
                    is HttpException -> {
                        when (e.code()) {
                            400 -> "Permintaan tidak valid"
                            404 -> "Email tidak ditemukan"
                            500 -> "Terjadi kesalahan server"
                            else -> "Gagal memperbarui kata sandi: ${e.message()}"
                        }
                    }
                    else -> "Error: ${e.message}"
                }
                Toast.makeText(this@UbahKataSandiActivity, errorMessage, Toast.LENGTH_LONG).show()
            }
        }
    }
}