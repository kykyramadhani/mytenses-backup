package com.app.mytenses.Activity

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatRadioButton
import com.app.mytenses.R
import com.google.firebase.database.*

class QuizActivity : AppCompatActivity() {

    private lateinit var databaseRef: DatabaseReference
    private lateinit var tvQuestion: TextView
    private lateinit var optionsA: TextView
    private lateinit var optionsB: TextView
    private lateinit var optionsC: TextView
    private lateinit var optionsD: TextView
    private lateinit var rbA: AppCompatRadioButton
    private lateinit var rbB: AppCompatRadioButton
    private lateinit var rbC: AppCompatRadioButton
    private lateinit var rbD: AppCompatRadioButton
    private lateinit var btnNext: Button
    private lateinit var btnBack: Button

    private var questionsList = mutableListOf<Question>()
    private var questionIndex = 0
    private var userAnswers = mutableMapOf<Int, String>()
    private var score = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        // Inisialisasi Firebase
        databaseRef = FirebaseDatabase.getInstance().getReference("questions")

        // Inisialisasi view
        tvQuestion = findViewById(R.id.tvQuestion)
        optionsA = findViewById(R.id.optionsAText)
        optionsB = findViewById(R.id.optionsBText)
        optionsC = findViewById(R.id.optionsCText)
        optionsD = findViewById(R.id.optionsDText)
        rbA = findViewById(R.id.rbOptionA2)
        rbB = findViewById(R.id.rbOptionB)
        rbC = findViewById(R.id.rbOptionC)
        rbD = findViewById(R.id.rbOptionD)
        btnNext = findViewById(R.id.btnNexOrLihatJawaban)
        btnBack = findViewById(R.id.btnBack)

        // Ambil data soal dari Firebase
        fetchQuestions()

        // Tombol Next
        btnNext.setOnClickListener {
            saveUserAnswer()
            if (questionIndex < questionsList.size - 1) {
                questionIndex++
                showQuestion()
            } else {
                hitungSkor()
            }
        }

        // Tombol Back
        btnBack.setOnClickListener {
            if (questionIndex > 0) {
                questionIndex--
                showQuestion()
            }
        }
    }

    private fun fetchQuestions() {
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                questionsList.clear()
                for (data in snapshot.children) {
                    val questionText = data.child("text").getValue(String::class.java) ?: ""
                    val correctOptionKey = data.child("correct_option").getValue(String::class.java) ?: ""

                    val optionsSnapshot = data.child("options")
                    val optionsMap = mutableMapOf<String, String>()
                    val labelMap = mapOf("0" to "A", "1" to "B", "2" to "C", "3" to "D")

                    for (option in optionsSnapshot.children) {
                        val label = labelMap[option.key] ?: continue
                        val value = option.getValue(String::class.java) ?: ""
                        optionsMap[label] = value
                    }

                    val correctOptionValue = optionsMap[labelMap[correctOptionKey]] ?: ""

                    val question = Question(
                        text = questionText,
                        options = optionsMap,
                        correct_option = correctOptionValue
                    )
                    questionsList.add(question)
                }
                showQuestion()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@QuizActivity, "Gagal ambil soal", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showQuestion() {
        if (questionIndex in questionsList.indices) {
            val current = questionsList[questionIndex]
            tvQuestion.text = current.text
            optionsA.text = current.options["A"]
            optionsB.text = current.options["B"]
            optionsC.text = current.options["C"]
            optionsD.text = current.options["D"]

            // Reset pilihan
            rbA.isChecked = false
            rbB.isChecked = false
            rbC.isChecked = false
            rbD.isChecked = false

            // Tampilkan jawaban sebelumnya jika ada
            when (userAnswers[questionIndex]) {
                current.options["A"] -> rbA.isChecked = true
                current.options["B"] -> rbB.isChecked = true
                current.options["C"] -> rbC.isChecked = true
                current.options["D"] -> rbD.isChecked = true
            }

            btnBack.visibility = if (questionIndex == 0) View.GONE else View.VISIBLE
            btnNext.text = if (questionIndex == questionsList.size - 1) "Lihat Skor" else "Next"
        }
    }

    private fun saveUserAnswer() {
        val selectedAnswer = when {
            rbA.isChecked -> optionsA.text.toString()
            rbB.isChecked -> optionsB.text.toString()
            rbC.isChecked -> optionsC.text.toString()
            rbD.isChecked -> optionsD.text.toString()
            else -> ""
        }

        if (selectedAnswer.isNotEmpty()) {
            userAnswers[questionIndex] = selectedAnswer
        }
    }

    private fun hitungSkor() {
        score = 0
        for ((index, question) in questionsList.withIndex()) {
            val jawaban = userAnswers[index]
            if (jawaban == question.correct_option) {
                score++
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Skor Kamu")
            .setMessage("Jawaban benar: $score dari ${questionsList.size}")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    data class Question(
        val text: String = "",
        val options: Map<String, String> = emptyMap(),
        val correct_option: String = ""
    )
}
