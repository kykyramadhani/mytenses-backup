package com.app.mytenses.Activity

import CourseRingkasanSimplePresentFragment
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.mytenses.Course
import com.app.mytenses.CourseAdapter
import com.app.mytenses.R
import com.app.mytenses.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CourseFragment : Fragment() {

    private lateinit var adapter: CourseAdapter
    private lateinit var username: String
    private val apiService = RetrofitClient.apiService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_course, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get username from SharedPreferences
        val sharedPreferences = requireActivity().getSharedPreferences("MyTensesPrefs", Context.MODE_PRIVATE)
        username = "user_${sharedPreferences.getInt("user_id", -1)}"
        Log.d(TAG, "SharedPreferences - username: $username")

        // RecyclerView setup
        val rvCourses = view.findViewById<RecyclerView>(R.id.rvCourses)
        if (rvCourses != null) {
            rvCourses.layoutManager = LinearLayoutManager(view.context)

            // Initialize default courses
            val defaultCourseList = listOf(
                Course("Simple Present", "Belum Mulai", 0, R.drawable.simple_present, "simple_present"),
                Course("Simple Past", "Belum Mulai", 0, R.drawable.simple_past, "simple_past"),
                Course("Simple Future", "Belum Mulai", 0, R.drawable.simple_future, "simple_future"),
                Course("Simple Past Future", "Belum Mulai", 0, R.drawable.simple_past_future, "simple_past_future")
            )

            adapter = CourseAdapter(defaultCourseList)
            rvCourses.adapter = adapter

            adapter.setOnItemClickListener(object : CourseAdapter.OnItemClickListener {
                override fun onItemClick(position: Int) {
                    val course = adapter.getItem(position)
                    val fragment = CourseRingkasanSimplePresentFragment().apply {
                        arguments = Bundle().apply {
                            putString("TITLE", course.title)
                            putString("STATUS", course.status)
                            putInt("PROGRESS", course.progress)
                            putInt("IMAGE_RES_ID", course.iconResId)
                            putString("LESSON_ID", course.lessonId)
                        }
                    }
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit()
                }
            })

            // Fetch progress for all courses
            fetchAllLessonProgress()
        } else {
            Log.e(TAG, "rvCourses is null, check layout file!")
        }
    }

    private fun fetchAllLessonProgress() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.getLessonProgress(username)
                }
                if (response.isSuccessful) {
                    val lessonProgressList = response.body()?.lesson_progress ?: emptyList()
                    // Initialize default courses
                    val updatedCourses = mutableListOf<Course>().apply {
                        add(Course("Simple Present", "Belum Mulai", 0, R.drawable.simple_present, "simple_present"))
                        add(Course("Simple Past", "Belum Mulai", 0, R.drawable.simple_past, "simple_past"))
                        add(Course("Simple Future", "Belum Mulai", 0, R.drawable.simple_future, "simple_future"))
                        add(Course("Simple Past Future", "Belum Mulai", 0, R.drawable.simple_past_future, "simple_past_future"))
                    }
                    // Update courses with API data if available
                    lessonProgressList.forEach { progressItem ->
                        val index = updatedCourses.indexOfFirst { it.lessonId == progressItem.lesson_id }
                        if (index != -1) {
                            updatedCourses[index] = Course(
                                title = progressItem.title,
                                status = when (progressItem.status) {
                                    "not_started" -> "Belum Mulai"
                                    "in_progress" -> "Sedang Diproses"
                                    "completed" -> "Selesai"
                                    else -> "Belum Mulai"
                                },
                                progress = progressItem.progress,
                                iconResId = updatedCourses[index].iconResId,
                                lessonId = progressItem.lesson_id
                            )
                        }
                    }
                    adapter.updateData(updatedCourses.sortedBy { it.title })
                } else {
                    Log.e(TAG, "Failed to fetch progress: ${response.message()}")
                    // Keep default courses if API call fails
                    adapter.updateData(listOf(
                        Course("Simple Present", "Belum Mulai", 0, R.drawable.simple_present, "simple_present"),
                        Course("Simple Past", "Belum Mulai", 0, R.drawable.simple_past, "simple_past"),
                        Course("Simple Future", "Belum Mulai", 0, R.drawable.simple_future, "simple_future"),
                        Course("Simple Past Future", "Belum Mulai", 0, R.drawable.simple_past_future, "simple_past_future")
                    ))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching progress: ${e.message}")
                // Keep default courses if exception occurs
                adapter.updateData(listOf(
                    Course("Simple Present", "Belum Mulai", 0, R.drawable.simple_present, "simple_present"),
                    Course("Simple Past", "Belum Mulai", 0, R.drawable.simple_past, "simple_past"),
                    Course("Simple Future", "Belum Mulai", 0, R.drawable.simple_future, "simple_future"),
                    Course("Simple Past Future", "Belum Mulai", 0, R.drawable.simple_past_future, "simple_past_future")
                ))
            }
        }
    }

    companion object {
        private const val TAG = "CourseFragment"
    }
}