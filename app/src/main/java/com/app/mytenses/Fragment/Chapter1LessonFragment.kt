package com.app.mytenses.Fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.app.mytenses.R
import com.app.mytenses.model.LessonProgress
import com.app.mytenses.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class Chapter1LessonFragment : Fragment() {

    private val TAG = "Chapter1LessonFragment"
    private lateinit var progressBar: ProgressBar
    private lateinit var frameCard1: FrameLayout
    private lateinit var frameCard2: FrameLayout
    private lateinit var frameCard3: FrameLayout
    private lateinit var tvMainTitle: TextView
    private lateinit var tvSubTitle: TextView
    private lateinit var textOnImage1: TextView
    private lateinit var textOnImage2: TextView
    private lateinit var textOnImage3: TextView
    private var lessonId: String? = null

    private val fragmentScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retrieve lessonId from arguments
        lessonId = arguments?.getString("lesson_id") ?: "simple_present"
        Log.d(TAG, "Lesson ID: $lessonId")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView called")
        return inflater.inflate(R.layout.fragment_chapter1_lesson, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<ImageButton>(R.id.btnBackChapt1)
        val btnNext = view.findViewById<Button>(R.id.btnNextChapt1)
        tvMainTitle = view.findViewById(R.id.tvMainTitle)
        tvSubTitle = view.findViewById(R.id.tvSubTitleLesson1)
        textOnImage1 = view.findViewById(R.id.textOnImage1)
        textOnImage2 = view.findViewById(R.id.textOnImage2)
        textOnImage3 = view.findViewById(R.id.textOnImage3)
        progressBar = view.findViewById(R.id.progressBar)
        frameCard1 = view.findViewById(R.id.frameCard1)
        frameCard2 = view.findViewById(R.id.frameCard2)
        frameCard3 = view.findViewById(R.id.frameCard3)

        progressBar.visibility = View.VISIBLE
        frameCard1.visibility = View.GONE
        frameCard2.visibility = View.GONE
        frameCard3.visibility = View.GONE

        fetchLessonData()

        btnBack?.setOnClickListener {
            Log.d(TAG, "Back button clicked")
            requireActivity().supportFragmentManager.popBackStack()
        }

        btnNext?.setOnClickListener {
            Log.d(TAG, "Next button clicked")
            fragmentScope.launch {
                val username = getUsername()
                val lesson = lessonId ?: "simple_present"
                if (username.isNotBlank() && lesson.isNotBlank()) {
                    updateLessonProgress(username, lesson, 25, "in_progress", 2)
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container,
                            Chapter2FormulaFragment.newInstance(lesson)
                        )
                        .addToBackStack(null)
                        .commit()
                } else {
                    Log.e(TAG, "Invalid username: $username or lessonId: $lesson")
                    Toast.makeText(requireContext(), "Error: Username atau Lesson ID tidak valid", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getUsername(): String {
        val sharedPreferences = requireContext().getSharedPreferences("MyTensesPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("user_id", -1)
        val username = sharedPreferences.getString("username", "user_$userId") ?: "user_$userId"
        Log.d(TAG, "User ID: $userId, Username: $username")
        return username
    }

    private fun fetchLessonData() {
        fragmentScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getMaterials()
                }

                if (response.isSuccessful) {
                    val materialsResponse = response.body()
                    Log.d(TAG, "API Response: $materialsResponse")

                    if (materialsResponse == null || materialsResponse.materials.isEmpty()) {
                        Log.e(TAG, "API response is null or empty")
                        showError("No material data received")
                        return@launch
                    }

                    val material = materialsResponse.materials.find { it.lesson_id == lessonId }
                    if (material == null) {
                        Log.e(TAG, "Material for '$lessonId' not found in response: ${materialsResponse.materials}")
                        showError("Material data not found")
                        return@launch
                    }

                    Log.d(TAG, "lesson_id: ${material.lesson_id}")
                    Log.d(TAG, "chapter_title: ${material.chapter_title}")
                    Log.d(TAG, "explanation: ${material.explanation}")

                    tvMainTitle.text = "Chapter 1"
                    tvSubTitle.text = material.chapter_title

                    val explanations = material.explanation ?: emptyList()
                    textOnImage1.text = explanations.getOrNull(0) ?: "No data"
                    Log.d(TAG, "Card 1: ${textOnImage1.text}")
                    textOnImage2.text = explanations.getOrNull(1) ?: "No data"
                    Log.d(TAG, "Card 2: ${textOnImage2.text}")
                    textOnImage3.text = explanations.getOrNull(2) ?: "No data"
                    Log.d(TAG, "Card 3: ${textOnImage3.text}")

                    progressBar.visibility = View.GONE
                    frameCard1.visibility = View.VISIBLE
                    frameCard2.visibility = View.VISIBLE
                    frameCard3.visibility = View.VISIBLE
                } else {
                    val errorMessage = "Error: ${response.code()} - ${response.message()}"
                    Log.e(TAG, "HTTP Error: $errorMessage")
                    showError(errorMessage)
                }
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is HttpException -> "HTTP Error: ${e.code()} - ${e.message()}"
                    is java.net.UnknownHostException -> "No internet connection"
                    else -> "Error loading data: ${e.message}"
                }
                Log.e(TAG, errorMessage, e)
                showError(errorMessage)
            }
        }
    }

    private fun showError(message: String) {
        textOnImage1.text = message
        textOnImage2.text = message
        textOnImage3.text = message
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        progressBar.visibility = View.GONE
        frameCard1.visibility = View.VISIBLE
        frameCard2.visibility = View.VISIBLE
        frameCard3.visibility = View.VISIBLE
    }

    private suspend fun updateLessonProgress(username: String, lessonId: String, newProgress: Int, status: String, retries: Int = 2) {
        repeat(retries + 1) { attempt ->
            try {
                Log.d(TAG, "Attempt ${attempt + 1}: Fetching progress for $username/$lessonId")
                val getResponse = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getLessonProgress(username)
                }
                Log.d(TAG, "GET Response: ${getResponse.code()} ${getResponse.message()}")
                var currentProgress = 0
                if (getResponse.isSuccessful) {
                    currentProgress = getResponse.body()?.lesson_progress?.find { it.lesson_id == lessonId }?.progress ?: 0
                    Log.d(TAG, "Current progress for $lessonId: $currentProgress")
                } else if (getResponse.code() == 404) {
                    Log.w(TAG, "User or lesson not found, assuming current progress is 0")
                } else {
                    Log.e(TAG, "Failed to fetch progress: ${getResponse.code()} - ${getResponse.message()}")
                    return@repeat
                }
                if (newProgress > currentProgress) {
                    Log.d(TAG, "Updating progress to $newProgress% - $status")
                    val updateResponse = withContext(Dispatchers.IO) {
                        RetrofitClient.apiService.updateLessonProgress(username, lessonId, LessonProgress(newProgress, status))
                    }
                    Log.d(TAG, "PUT Response: ${updateResponse.code()} ${updateResponse.message()}")
                    if (updateResponse.isSuccessful) {
                        Log.d(TAG, "Lesson progress updated: $newProgress% - $status")
                        return
                    } else {
                        Log.e(TAG, "Attempt ${attempt + 1} failed: ${updateResponse.code()} - ${updateResponse.message()}")
                    }
                } else {
                    Log.d(TAG, "No update needed: newProgress ($newProgress) <= currentProgress ($currentProgress)")
                    return
                }
            } catch (e: Exception) {
                Log.e(TAG, "Attempt ${attempt + 1} error: ${e.message}", e)
            }
            if (attempt < retries) delay(1000) // Wait 1s before retry
        }
        Toast.makeText(requireContext(), "Gagal memperbarui progres setelah beberapa percobaan", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragmentScope.cancel()
    }

    companion object {
        fun newInstance(lessonId: String = "simple_present"): Chapter1LessonFragment {
            return Chapter1LessonFragment().apply {
                arguments = Bundle().apply {
                    putString("lesson_id", lessonId)
                }
            }
        }
    }
}