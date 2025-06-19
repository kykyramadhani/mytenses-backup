package com.app.mytenses.Activity

import android.content.Intent
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

class QuizActivity : AppCompatActivity() {

    private lateinit var tvQuestion: TextView
    private lateinit var optionButtons: List<AppCompatButton>
    private lateinit var optionTexts: List<TextView>
    private lateinit var btnNext: Button
    private lateinit var btnBack: Button
    private lateinit var btnFinish: Button
    private var questionsList: List<Question> = emptyList()
    private var questionIndex = 0
    private var selectedOptionIndex = -1
    private val selectedAnswers = mutableMapOf<Int, Int>()
    private var isAnswerMode = true
    private lateinit var quizNumberButtons: List<AppCompatButton>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        tvQuestion = findViewById(R.id.tvQuestion)
        btnNext = findViewById(R.id.btnNextQuestion)
        btnBack = findViewById(R.id.btnBackQuestion)
        btnFinish = findViewById(R.id.btnFinish)

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

        // Inisialisasi tombol nomor soal
        quizNumberButtons = listOf(
            findViewById(R.id.quizNumber1),
            findViewById(R.id.quizNumber2),
            findViewById(R.id.quizNumber3),
            findViewById(R.id.quizNumber4),
            findViewById(R.id.quizNumber5)
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
            if (!isAnswerMode) {
                if (questionIndex < questionsList.size - 1) {
                    questionIndex++
                    isAnswerMode = true
                    showQuestion()
                } else {
                    Log.d("QuizActivity", "Sudah di soal terakhir di mode review")
                    Toast.makeText(this@QuizActivity, "Ini soal terakhir", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnBack.setOnClickListener {
            if (isAnswerMode) {
                if (questionIndex > 0) {
                    questionIndex--
                    isAnswerMode = false
                    showReviewQuestion()
                } else {
                    Log.d("QuizActivity", "Sudah di soal pertama. Index: $questionIndex")
                    Toast.makeText(this@QuizActivity, "Ini soal pertama", Toast.LENGTH_SHORT).show()
                }
            } else {
                if (questionIndex > 0) {
                    questionIndex--
                    showReviewQuestion()
                } else {
                    Log.d("QuizActivity", "Sudah di soal pertama di mode review")
                    Toast.makeText(this@QuizActivity, "Ini soal pertama", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Tombol Lihat Jawaban/Selesai
        btnFinish.setOnClickListener {
            Log.d("QuizActivity", "btnFinish diklik, isAnswerMode: $isAnswerMode, questionIndex: $questionIndex, questionsList.size: ${questionsList.size}")
            if (isAnswerMode) {
                if (selectedOptionIndex != -1) {
                    saveSelectedAnswer()
                    isAnswerMode = false
                    showReviewQuestion()
                } else {
                    Toast.makeText(this@QuizActivity, "Silakan pilih jawaban terlebih dahulu!", Toast.LENGTH_SHORT).show()
                }
            } else {
                if (questionIndex == questionsList.size - 1) {
                    val totalScore = calculateScore()
                    Log.d("QuizActivity", "Total Skor: $totalScore")
                    Log.d("QuizActivity", "Navigasi ke QuizResultFragment, isFinishing: ${isFinishing()}")
                    val fragment = QuizResultFragment.newInstance(totalScore)
                    try {
                        // Atur visibilitas
                        findViewById<LinearLayout>(R.id.quiz_ui_container).visibility = View.GONE
                        findViewById<FrameLayout>(R.id.fragment_container).visibility = View.VISIBLE
                        // Lakukan transaksi fragment
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack(null)
                            .commit()
                        Log.d("QuizActivity", "Transaksi fragment berhasil dikomit")
                    } catch (e: Exception) {
                        Log.e("QuizActivity", "Gagal melakukan transaksi fragment: ${e.message}", e)
                    }
                } else {
                    questionIndex++
                    isAnswerMode = true
                    showQuestion()
                }
            }
        }

        // Pilihan jawaban
        optionButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                selectedOptionIndex = index
                Log.d("QuizActivity", "Opsi yang dipilih: Index $index")
                updateOptionSelection()
                if (isAnswerMode) {
                    saveSelectedAnswer()
                }
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

        selectedAnswers[questionIndex]?.let { index ->
            selectedOptionIndex = index
            updateOptionSelection()
        }

        optionButtons.forEachIndexed { index, button ->
            button.isSelected = false
            if (index < currentQuestion.options.size) {
                button.visibility = View.VISIBLE
                optionTexts[index].visibility = View.VISIBLE
                button.text = ('A' + index).toString()
                optionTexts[index].text = currentQuestion.options[index]
                button.setBackgroundResource(R.drawable.quiz_number)
                Log.d("QuizActivity", "Opsi $index: ${currentQuestion.options[index]}")
            } else {
                button.visibility = View.GONE
                optionTexts[index].visibility = View.GONE
                Log.d("QuizActivity", "Opsi $index disembunyikan (tidak ada)")
            }
        }

        // Atur visibilitas dan status tombol nomor soal
        quizNumberButtons.forEachIndexed { index, button ->
            button.isSelected = index == questionIndex
            if (index < questionsList.size) {
                button.visibility = View.VISIBLE
            } else {
                button.visibility = View.GONE
            }
        }

        // Atur visibilitas tombol berdasarkan mode dan indeks
        btnBack.visibility = if (isAnswerMode && questionIndex > 0) View.VISIBLE else View.GONE
        btnNext.visibility = View.GONE
        btnFinish.visibility = View.VISIBLE
        btnFinish.text = "Lihat Jawaban"
    }

    private fun showReviewQuestion() {
        if (questionsList.isEmpty()) return

        val currentQuestion = questionsList[questionIndex]
        tvQuestion.text = "${currentQuestion.text}"

        val selectedIndex = selectedAnswers[questionIndex] ?: -1
        val correctOption = currentQuestion.options.indexOf(currentQuestion.correct_option)

        optionButtons.forEachIndexed { index, button ->
            if (index < currentQuestion.options.size) {
                button.visibility = View.VISIBLE
                optionTexts[index].visibility = View.VISIBLE
                button.text = ('A' + index).toString()
                optionTexts[index].text = currentQuestion.options[index]

                // Gunakan drawable dengan bentuk bulat dan warna sesuai
                when {
                    index == selectedIndex && index == correctOption -> button.setBackgroundResource(R.drawable.quiz_number_green)
                    index == selectedIndex -> button.setBackgroundResource(R.drawable.quiz_number_red)
                    index == correctOption -> button.setBackgroundResource(R.drawable.quiz_number_green)
                    else -> button.setBackgroundResource(R.drawable.quiz_number_gray)
                }
            } else {
                button.visibility = View.GONE
                optionTexts[index].visibility = View.GONE
            }
        }

        // Atur visibilitas dan status tombol nomor soal
        quizNumberButtons.forEachIndexed { index, button ->
            button.isSelected = index == questionIndex
            if (index < questionsList.size) {
                button.visibility = View.VISIBLE
            } else {
                button.visibility = View.GONE
            }
        }

        // Atur visibilitas tombol di mode review
        btnBack.visibility = View.GONE
        btnNext.visibility = View.GONE
        btnFinish.visibility = View.VISIBLE
        btnFinish.text = if (questionIndex == questionsList.size - 1) "Selesai" else "Next"
    }

    private fun updateOptionSelection() {
        optionButtons.forEachIndexed { index, button ->
            button.isSelected = index == selectedOptionIndex
        }
    }

    private fun saveSelectedAnswer() {
        if (selectedOptionIndex != -1) {
            selectedAnswers[questionIndex] = selectedOptionIndex
            Log.d("QuizActivity", "Jawaban disimpan untuk soal $questionIndex: Indeks $selectedOptionIndex")
        }
    }

    private fun calculateScore(): Int {
        var totalScore = 0
        for (i in questionsList.indices) {
            val selectedIndex = selectedAnswers[i] ?: -1
            val correctOption = questionsList[i].options.indexOf(questionsList[i].correct_option)
            if (selectedIndex == correctOption) {
                totalScore += questionsList[i].points
                Log.d("QuizActivity", "Soal $i benar, menambahkan ${questionsList[i].points} poin")
            } else {
                Log.d("QuizActivity", "Soal $i salah, jawaban: $selectedIndex, benar: $correctOption")
            }
        }
        return totalScore
    }

}