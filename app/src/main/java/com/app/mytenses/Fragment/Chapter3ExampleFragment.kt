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
import java.net.UnknownHostException

class Chapter3ExampleFragment : Fragment() {

    private val TAG = "Chapter3ExampleFragment"
    private lateinit var progressBar: ProgressBar
    private lateinit var frameCard1: FrameLayout
    private lateinit var frameCard2: FrameLayout
    private lateinit var tvMainTitle: TextView
    private lateinit var tvSubTitle: TextView
    private lateinit var textOnImage1: TextView
    private lateinit var textOnImage2: TextView
    private var lessonId: String? = null

    private val fragmentScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lessonId = arguments?.getString("lesson_id") ?: "simple_present"
        Log.d(TAG, "Lesson ID: $lessonId")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView called")
        return inflater.inflate(R.layout.fragment_chapter3_example, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<ImageButton>(R.id.btnBackChapt3)
        val btnNext = view.findViewById<Button>(R.id.btnNextChapt3)
        tvMainTitle = view.findViewById(R.id.tvMainTitle)
        tvSubTitle = view.findViewById(R.id.tvSubTitle)
        textOnImage1 = view.findViewById(R.id.textOnImage1)
        textOnImage2 = view.findViewById(R.id.textOnImage2)
        progressBar = view.findViewById(R.id.progressBar)
        frameCard1 = view.findViewById(R.id.frameCard1)
        frameCard2 = view.findViewById(R.id.frameCard2)

        progressBar.visibility = View.VISIBLE
        frameCard1.visibility = View.GONE
        frameCard2.visibility = View.GONE

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
                val fragment = QuizStartFragment.newInstance("simple_present")
                if (username.isNotBlank() && lesson.isNotBlank()) {
                    updateLessonProgress(username, lesson, 75, "in_progress", 2)
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
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
                        showError("Material data not found for $lessonId")
                        return@launch
                    }

                    Log.d(TAG, "lesson_id: ${material.lesson_id}")
                    Log.d(TAG, "chapter_title: ${material.chapter_title}")
                    Log.d(TAG, "examples: ${material.examples}")

                    // Set titles
                    tvMainTitle.text = "Chapter 3"
                    tvSubTitle.text = "Contoh ${getLessonTitle(lessonId)}"

                    // Display examples
                    val examples = material.examples ?: emptyList()
                    textOnImage1.text = examples.getOrNull(0)?.let { "${it.sentence}\n${it.example_translation}" } ?: "No example available"
                    Log.d(TAG, "Card 1: ${textOnImage1.text}")
                    textOnImage2.text = examples.getOrNull(1)?.let { "${it.sentence}\n${it.example_translation}" } ?: "No example available"
                    Log.d(TAG, "Card 2: ${textOnImage2.text}")

                    progressBar.visibility = View.GONE
                    frameCard1.visibility = View.VISIBLE
                    frameCard2.visibility = View.VISIBLE
                } else {
                    val errorMessage = "Error: ${response.code()} - ${response.message()}"
                    Log.e(TAG, "HTTP Error: $errorMessage")
                    showError(errorMessage)
                }
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is HttpException -> "HTTP Error: ${e.code()} - ${e.message()}"
                    is UnknownHostException -> "No internet connection"
                    else -> "Error loading data: ${e.message}"
                }
                Log.e(TAG, errorMessage, e)
                showError(errorMessage)
            }
        }
    }

    private fun getLessonTitle(lessonId: String?): String {
        return when (lessonId) {
            "simple_present" -> "Simple Present"
            "simple_past" -> "Simple Past"
            "simple_future" -> "Simple Future"
            "simple_past_future" -> "Simple Past Future"
            "present_continuous" -> "Present Continuous"
            "past_continuous" -> "Past Continuous"
            "future_continuous" -> "Future Continuous"
            "past_future_continuous" -> "Past Future Continuous"
            "present_perfect" -> "Present Perfect"
            "past_perfect" -> "Past Perfect"
            "future_perfect" -> "Future Perfect"
            "past_future_perfect" -> "Past Future Perfect"
            "present_perfect_continuous" -> "Present Perfect Continuous"
            "past_perfect_continuous" -> "Past Perfect Continuous"
            "future_perfect_continuous" -> "Future Perfect Continuous"
            "past_future_perfect_continuous" -> "Past Future Perfect Continuous"
            else -> "Unknown Lesson"
        }
    }

    private fun showError(message: String) {
        textOnImage1.text = message
        textOnImage2.text = message
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        progressBar.visibility = View.GONE
        frameCard1.visibility = View.VISIBLE
        frameCard2.visibility = View.VISIBLE
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
        withContext(Dispatchers.Main) {
            Toast.makeText(requireContext(), "Gagal memperbarui progres setelah beberapa percobaan", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragmentScope.cancel()
        Log.d(TAG, "onDestroyView: Coroutine scope cancelled")
    }

    companion object {
        fun newInstance(lessonId: String = "simple_present"): Chapter3ExampleFragment {
            return Chapter3ExampleFragment().apply {
                arguments = Bundle().apply {
                    putString("lesson_id", lessonId)
                }
            }
        }
    }
}