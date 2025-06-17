package com.app.mytenses.Activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import com.app.mytenses.R
import com.app.mytenses.model.Question
import com.app.mytenses.network.RetrofitClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class QuizActivity : AppCompatActivity() {

    private lateinit var tvQuestion: TextView
    private lateinit var optionButtons: List<AppCompatButton>
    private lateinit var optionTexts: List<TextView>
    private lateinit var btnNext: Button
    private lateinit var btnBack: Button
    private var questionsList: List<Question> = emptyList()
    private var questionIndex = 0
    private var selectedOptionIndex = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        tvQuestion = findViewById(R.id.tvQuestion)
        btnNext = findViewById(R.id.btnNextQuestion)
        btnBack = findViewById(R.id.btnBackQuestion)

        optionButtons = listOf(
            findViewById(R.id.rbOptionA),
            findViewById(R.id.rbOptionB),
            findViewById(R.id.rbOptionC),
            findViewById(R.id.rbOptionD)
        )

        optionTexts = listOf(
            findViewById(R.id.optionsAText),
            findViewById(R.id.optionsBText),
            findViewById(R.id.optionsCText),
            findViewById(R.id.optionsDText)
        )

        // Tombol back dari header
        findViewById<ImageButton>(R.id.btnBackQuizSP)?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Fetch soal dari API dalam coroutine
        lifecycleScope.launch {
            fetchQuestionsFromApi(Dispatchers.IO)
        }

        // Navigasi soal
        btnNext.setOnClickListener {
            if (questionIndex < questionsList.size - 1) {
                questionIndex++
                Log.d("QuizActivity", "Tombol Next diklik. Index sekarang: $questionIndex, Jumlah soal: ${questionsList.size}")
                showQuestion()
            } else {
                Log.d("QuizActivity", "Sudah di soal terakhir. Index: $questionIndex")
                Toast.makeText(this@QuizActivity, "Ini soal terakhir", Toast.LENGTH_SHORT).show()
            }
        }

        btnBack.setOnClickListener {
            if (questionIndex > 0) {
                questionIndex--
                Log.d("QuizActivity", "Tombol Back diklik. Index sekarang: $questionIndex")
                showQuestion()
            } else {
                Log.d("QuizActivity", "Sudah di soal pertama. Index: $questionIndex")
                Toast.makeText(this@QuizActivity, "Ini soal pertama", Toast.LENGTH_SHORT).show()
            }
        }

        // Pilihan jawaban
        optionButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                selectedOptionIndex = index
                Log.d("QuizActivity", "Opsi yang dipilih: Index $index")
                updateOptionSelection()
            }
        }
    }

    private suspend fun fetchQuestionsFromApi(dispatcher: CoroutineDispatcher = Dispatchers.IO) {
        try {
            Log.d("QuizActivity", "Memulai panggilan API ke endpoint: api/questions")
            val response = withContext(dispatcher) {
                RetrofitClient.apiService.getQuestions()
            }
            Log.d("QuizActivity", "Respons diterima: ${response.raw()}")

            if (response.isSuccessful) {
                val body = response.body()
                Log.d("QuizActivity", "API Response Body: $body")

                if (body == null) {
                    Log.e("QuizActivity", "API response body is null")
                    runOnUiThread {
                        Toast.makeText(this@QuizActivity, "Tidak ada soal tersedia", Toast.LENGTH_LONG).show()
                    }
                    return
                }

                // Ekstrak daftar questions dari body
                @Suppress("UNCHECKED_CAST")
                val questionsMap = body as? Map<String, List<Question>>
                val questions = questionsMap?.get("questions") ?: emptyList()

                if (questions.isEmpty()) {
                    Log.e("QuizActivity", "API response questions is empty")
                    runOnUiThread {
                        Toast.makeText(this@QuizActivity, "Tidak ada soal tersedia", Toast.LENGTH_LONG).show()
                    }
                    return
                }

                questionsList = questions
                Log.d("QuizActivity", "Jumlah soal berhasil dimuat: ${questionsList.size}")
                questionsList.forEachIndexed { i, q ->
                    Log.d("QuizActivity", "Soal ke-$i: ${q.text}, Opsi: ${q.options.joinToString()}")
                }

                runOnUiThread {
                    showQuestion()
                }
            } else {
                val errorMessage = "Error: ${response.code()} - ${response.message()}"
                Log.e("QuizActivity", "HTTP Error: $errorMessage")
                Log.e("QuizActivity", "Error body: ${response.errorBody()?.string()}")
                runOnUiThread {
                    Toast.makeText(this@QuizActivity, "Soal tidak tersedia: $errorMessage", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            val errorMessage = when (e) {
                is retrofit2.HttpException -> "HTTP Error: ${e.code()} - ${e.message()}"
                is java.net.UnknownHostException -> "Tidak ada koneksi internet: ${e.message}"
                else -> "Error memuat data: ${e.message}"
            }
            Log.e("QuizActivity", errorMessage, e)
            runOnUiThread {
                Toast.makeText(this@QuizActivity, "Gagal memuat soal: $errorMessage", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showQuestion() {
        if (questionsList.isEmpty()) {
            Log.d("QuizActivity", "Daftar soal kosong, tidak ada yang ditampilkan")
            return
        }

        val currentQuestion = questionsList[questionIndex]
        Log.d("QuizActivity", "Menampilkan soal index: $questionIndex -> ${currentQuestion.text}")

        tvQuestion.text = currentQuestion.text
        selectedOptionIndex = -1

        optionButtons.forEachIndexed { index, button ->
            button.isSelected = false
            if (index < currentQuestion.options.size) {
                button.visibility = View.VISIBLE
                optionTexts[index].visibility = View.VISIBLE
                button.text = ('A' + index).toString()
                optionTexts[index].text = currentQuestion.options[index]
                Log.d("QuizActivity", "Opsi $index: ${currentQuestion.options[index]}")
            } else {
                button.visibility = View.GONE
                optionTexts[index].visibility = View.GONE
                Log.d("QuizActivity", "Opsi $index disembunyikan (tidak ada)")
            }
        }

        btnBack.visibility = if (questionIndex == 0) View.INVISIBLE else View.VISIBLE
        btnNext.visibility = if (questionIndex == questionsList.size - 1) View.INVISIBLE else View.VISIBLE
    }

    private fun updateOptionSelection() {
        optionButtons.forEachIndexed { index, button ->
            button.isSelected = index == selectedOptionIndex
        }
    }
}