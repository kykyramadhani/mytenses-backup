package com.app.mytenses.Fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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

class CompletedCourseFragment : Fragment() {

    private val TAG = "CompletedCourseFragment"
    private lateinit var textViewDesc: TextView
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
        return inflater.inflate(R.layout.fragment_completed_course, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<Button>(R.id.btnBackCompleted)
        textViewDesc = view.findViewById(R.id.textViewDesc)

        textViewDesc.text = "kelas ${lessonId?.replace("_", " ")?.capitalize()}!"

        btnBack?.setOnClickListener {
            Log.d(TAG, "Back button clicked")
            fragmentScope.launch {
                val username = getUsername()
                val lesson = lessonId ?: "simple_present"
                if (username.isNotBlank() && lesson.isNotBlank()) {
                    updateLessonProgress(username, lesson, 100, "completed", 2)
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HomeFragment())
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
        fun newInstance(lessonId: String = "simple_present"): CompletedCourseFragment {
            return CompletedCourseFragment().apply {
                arguments = Bundle().apply {
                    putString("lesson_id", lessonId)
                }
            }
        }
    }
}