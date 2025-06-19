package com.app.mytenses.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.app.mytenses.Activity.HomeFragment
import com.app.mytenses.R
import com.app.mytenses.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CompletedCourseFragment : Fragment() {

    private val TAG = "CompletedCourseFragment"
    private val fragmentScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_completed_course, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBackCompleted = view.findViewById<Button>(R.id.btnBackCompleted)
        val textViewDesc = view.findViewById<TextView>(R.id.textViewDesc)

        btnBackCompleted?.setOnClickListener {
            Log.d(TAG, "Back completed button clicked")
            fragmentScope.launch {
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, HomeFragment())
                    .commit()
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val lessonId = arguments?.getString("lesson_id") ?: "simple_present"
        Log.d(TAG, "lessonId received: $lessonId")


        textViewDesc?.text = getString(R.string.congrats_message, "")


        fragmentScope.launch {
            val title = getLessonTitle(lessonId)
            Log.d(TAG, "Fetched title: $title")
            textViewDesc?.text = getString(R.string.congrats_message, title ?: "Unknown")
        }
    }

    private suspend fun getLessonTitle(lessonId: String): String? {
        return try {
            val response = withContext(Dispatchers.IO) {
                RetrofitClient.apiService.getMaterials()
            }

            if (response.isSuccessful) {
                val materialsResponse = response.body()
                val material = materialsResponse?.materials?.find { it.lesson_id == lessonId }
                material?.chapter_title
            } else {
                Log.e(TAG, "API error: ${response.code()} - ${response.message()}")
                Toast.makeText(requireContext(), "Gagal mengambil data pelajaran", Toast.LENGTH_LONG).show()
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while fetching lesson: ${e.message}", e)
            Toast.makeText(requireContext(), "Terjadi kesalahan jaringan", Toast.LENGTH_LONG).show()
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragmentScope.cancel()
    }

    companion object {
        fun newInstance(lessonId: String): CompletedCourseFragment {
            return CompletedCourseFragment().apply {
                arguments = Bundle().apply {
                    putString("lesson_id", lessonId)
                }
            }
        }
    }
}