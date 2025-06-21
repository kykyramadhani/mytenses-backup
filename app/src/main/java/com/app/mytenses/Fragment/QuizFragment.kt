package com.app.mytenses.Fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.app.mytenses.R
import com.app.mytenses.model.Question
import com.app.mytenses.network.RetrofitClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QuizFragment : Fragment() {

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_quiz, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        tvQuestion = view.findViewById(R.id.tvQuestion)
        btnNext = view.findViewById(R.id.btnNextQuestion)
        btnBack = view.findViewById(R.id.btnBackQuestion)
        btnFinish = view.findViewById(R.id.btnFinish)

        optionButtons = listOf(
            view.findViewById(R.id.rbOptionA),
            view.findViewById(R.id.rbOptionB),
            view.findViewById(R.id.rbOptionC),
            view.findViewById(R.id.rbOptionD)
        )

        optionTexts = listOf(
            view.findViewById(R.id.optionsAText),
            view.findViewById(R.id.optionsBText),
            view.findViewById(R.id.optionsCText),
            view.findViewById(R.id.optionsDText)
        )

        quizNumberButtons = listOf(
            view.findViewById(R.id.quizNumber1),
            view.findViewById(R.id.quizNumber2),
            view.findViewById(R.id.quizNumber3),
            view.findViewById(R.id.quizNumber4),
            view.findViewById(R.id.quizNumber5)
        )

        view.findViewById<ImageButton>(R.id.btnBackQuizSP)?.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            fetchQuestionsFromApi()
        }

        btnNext.setOnClickListener {
            if (!isAnswerMode && questionIndex < questionsList.size - 1) {
                questionIndex++
                isAnswerMode = true
                showQuestion()
            }
        }

        btnBack.setOnClickListener {
            if (isAnswerMode && questionIndex > 0) {
                questionIndex--
                isAnswerMode = false
                showReviewQuestion()
            } else if (!isAnswerMode && questionIndex > 0) {
                questionIndex--
                showReviewQuestion()
            }
        }

        btnFinish.setOnClickListener {
            if (isAnswerMode) {
                if (selectedOptionIndex != -1) {
                    saveSelectedAnswer()
                    isAnswerMode = false
                    showReviewQuestion()
                } else {
                    Toast.makeText(requireContext(), "Silakan pilih jawaban terlebih dahulu!", Toast.LENGTH_SHORT).show()
                }
            } else {
                if (questionIndex == questionsList.size - 1) {
                    val totalScore = calculateScore()
                    val fragment = QuizResultFragment.newInstance(totalScore)
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit()

                    submitQuizScore(totalScore) // <- tambahkan ini
                }
                else {
                    questionIndex++
                    isAnswerMode = true
                    showQuestion()
                }
            }
        }

        optionButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                selectedOptionIndex = index
                updateOptionSelection()
                if (isAnswerMode) saveSelectedAnswer()
            }
        }
    }

    private suspend fun fetchQuestionsFromApi(dispatcher: CoroutineDispatcher = Dispatchers.IO) {
        val lessonId = arguments?.getString("lesson_id")
        val quizId = when (lessonId) {
            "simple_present" -> "quiz_simple_present_1"
            "simple_past" -> "quiz_simple_past_1"
            "simple_future" -> "quiz_simple_future_1"
            // Tambahkan semua mapping lain
            else -> null
        }

        if (quizId == null) {
            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), "Quiz ID tidak ditemukan untuk lesson ini", Toast.LENGTH_SHORT).show()
            }
            return
        }

        try {
            val response = withContext(dispatcher) {
                RetrofitClient.apiService.getQuestions()
            }
            if (response.isSuccessful) {
                val allQuestions = response.body()?.get("questions") ?: emptyList()
                questionsList = allQuestions.filter { it.quiz_id == quizId }

                if (questionsList.isEmpty()) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Soal tidak tersedia untuk quiz ini", Toast.LENGTH_LONG).show()
                    }
                } else {
                    requireActivity().runOnUiThread {
                        showQuestion()
                    }
                }
            } else {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Soal tidak tersedia", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), "Gagal memuat soal", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun showQuestion() {
        if (questionsList.isEmpty()) return
        val currentQuestion = questionsList[questionIndex]
        tvQuestion.text = currentQuestion.text
        selectedOptionIndex = selectedAnswers[questionIndex] ?: -1
        updateOptionSelection()

        optionButtons.forEachIndexed { index, button ->
            if (index < currentQuestion.options.size) {
                button.visibility = View.VISIBLE
                optionTexts[index].visibility = View.VISIBLE
                button.text = ('A' + index).toString()
                optionTexts[index].text = currentQuestion.options[index]
                button.setBackgroundResource(R.drawable.quiz_number)
            } else {
                button.visibility = View.GONE
                optionTexts[index].visibility = View.GONE
            }
        }

        quizNumberButtons.forEachIndexed { index, button ->
            button.isSelected = index == questionIndex
            button.visibility = if (index < questionsList.size) View.VISIBLE else View.GONE
        }

        btnBack.visibility = if (isAnswerMode && questionIndex > 0) View.VISIBLE else View.GONE
        btnNext.visibility = View.GONE
        btnFinish.visibility = View.VISIBLE
        btnFinish.text = "Lihat Jawaban"
    }

    private fun showReviewQuestion() {
        val currentQuestion = questionsList[questionIndex]
        tvQuestion.text = currentQuestion.text

        val selectedIndex = selectedAnswers[questionIndex] ?: -1
        val correctOption = currentQuestion.options.indexOf(currentQuestion.correct_option)

        optionButtons.forEachIndexed { index, button ->
            if (index < currentQuestion.options.size) {
                button.visibility = View.VISIBLE
                optionTexts[index].visibility = View.VISIBLE
                button.text = ('A' + index).toString()
                optionTexts[index].text = currentQuestion.options[index]

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

        quizNumberButtons.forEachIndexed { index, button ->
            button.isSelected = index == questionIndex
            button.visibility = if (index < questionsList.size) View.VISIBLE else View.GONE
        }

        btnBack.visibility = View.GONE
        btnNext.visibility = View.GONE
        btnFinish.visibility = if (questionIndex == questionsList.size - 1) View.VISIBLE else View.VISIBLE
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
        }
    }

    private fun calculateScore(): Int {
        var totalScore = 0
        for (i in questionsList.indices) {
            val selectedIndex = selectedAnswers[i] ?: -1
            val correctOption = questionsList[i].options.indexOf(questionsList[i].correct_option)
            if (selectedIndex == correctOption) {
                totalScore += questionsList[i].points
            }
        }
        return totalScore
    }

    private fun submitQuizScore(score: Int) {
        val sharedPreferences = requireActivity().getSharedPreferences("MyTensesPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("user_id", -1)
        val username = if (userId != -1) "user_$userId" else ""

        // Ambil quiz_id dari soal pertama
        val quizId = questionsList.firstOrNull()?.quiz_id

        if (username == null) {
            Toast.makeText(requireContext(), "Username tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        if (quizId == null) {
            Toast.makeText(requireContext(), "Quiz_id tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        val requestBody: Map<String, Any> = mapOf(
            "user_id" to username,
            "quiz_id" to quizId,
            "score" to score,
            "date_taken" to java.time.LocalDateTime.now().toString()
        )

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.addQuizScore(requestBody)
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Skor berhasil disimpan", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Gagal menyimpan skor", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
