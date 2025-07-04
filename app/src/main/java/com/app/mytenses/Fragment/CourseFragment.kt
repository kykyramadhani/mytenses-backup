package com.app.mytenses.Fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.app.mytenses.Course
import com.app.mytenses.CourseAdapter
import com.app.mytenses.R
import com.app.mytenses.data.database.AppDatabase
import com.app.mytenses.data.repository.UserRepository
import com.app.mytenses.network.RetrofitClient
import com.app.mytenses.utils.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CourseFragment : Fragment() {

    private lateinit var adapter: CourseAdapter
    private lateinit var username: String
    private lateinit var userRepository: UserRepository
    private val apiService = RetrofitClient.apiService
    private var fetchProgressJob: Job? = null
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_course, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = view.context
        if (context == null) {
            Log.e(TAG, "Context is null, skipping initialization")
            return
        }

        // Inisialisasi UserRepository
        userRepository = UserRepository(apiService, context, AppDatabase.getDatabase(context).userDao())

        // Get username from SharedPreferences
        val sharedPreferences = requireActivity().getSharedPreferences("MyTensesPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("user_id", -1)
        username = if (userId != -1) "user_$userId" else ""
        Log.d(TAG, "SharedPreferences - username: $username")

        if (username.isEmpty()) {
            Log.w(TAG, "Invalid username, skipping initialization")
            return
        }

        // RecyclerView setup
        val rvCourses = view.findViewById<RecyclerView>(R.id.rvCourses)
        swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout) ?: run {
            Log.e(TAG, "swipeRefreshLayout not found in layout")
            return
        }
        if (rvCourses != null) {
            rvCourses.layoutManager = LinearLayoutManager(context)

            // Inisialisasi adapter tanpa data awal
            adapter = CourseAdapter(mutableListOf())
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

            // Tampilkan data lokal secara langsung
            viewLifecycleOwner.lifecycleScope.launch {
                val updatedCourses = mutableListOf<Course>().apply {
                    add(Course("Simple Present", "Belum Mulai", 0, R.drawable.simple_present, "simple_present"))
                    add(Course("Simple Past", "Belum Mulai", 0, R.drawable.simple_past, "simple_past"))
                    add(Course("Simple Future", "Belum Mulai", 0, R.drawable.simple_future, "simple_future"))
                    add(Course("Simple Past Future", "Belum Mulai", 0, R.drawable.simple_past_future, "simple_past_future"))
                }
                fetchLocalLessonProgress(updatedCourses)
                val filteredCourses = updatedCourses.filter {
                    it.progress > 0 && (it.status == "Sedang Diproses" || it.status == "Selesai")
                }.sortedBy { it.status == "Selesai" }
                adapter.updateData(filteredCourses)
            }

            // Setup SwipeRefreshLayout
            swipeRefreshLayout.setOnRefreshListener {
                viewLifecycleOwner.lifecycleScope.launch {
                    fetchAllLessonProgress()
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        } else {
            Log.e(TAG, "rvCourses is null, check layout file!")
        }
    }

    private suspend fun fetchAllLessonProgress() {
        fetchProgressJob?.cancel()
        fetchProgressJob = viewLifecycleOwner.lifecycleScope.launch {
            val context = view?.context ?: return@launch
            val updatedCourses = mutableListOf<Course>().apply {
                add(Course("Simple Present", "Belum Mulai", 0, R.drawable.simple_present, "simple_present"))
                add(Course("Simple Past", "Belum Mulai", 0, R.drawable.simple_past, "simple_past"))
                add(Course("Simple Future", "Belum Mulai", 0, R.drawable.simple_future, "simple_future"))
                add(Course("Simple Past Future", "Belum Mulai", 0, R.drawable.simple_past_future, "simple_past_future"))
            }

            try {
                if (NetworkUtils.isOnline(context)) {
                    Log.d(TAG, "Online: Fetching lesson progress from API")
                    val response = withContext(Dispatchers.IO) {
                        apiService.getLessonProgress(username)
                    }
                    if (response.isSuccessful) {
                        val lessonProgressList = response.body()?.lesson_progress ?: emptyList()
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
                        // Sinkronkan data ke database
                        withContext(Dispatchers.IO) {
                            userRepository.syncUserData(username)
                        }
                    } else {
                        Log.e(TAG, "Failed to fetch progress from API: ${response.code()} - ${response.message()}")
                    }
                }
                fetchLocalLessonProgress(updatedCourses)
                val filteredCourses = updatedCourses.filter {
                    it.progress > 0 && (it.status == "Sedang Diproses" || it.status == "Selesai")
                }.sortedBy { it.status == "Selesai" }
                if (isAdded) {
                    adapter.updateData(filteredCourses)
                }
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    Log.e(TAG, "Error fetching progress: ${e.message}", e)
                }
                fetchLocalLessonProgress(updatedCourses)
                val filteredCourses = updatedCourses.filter {
                    it.progress > 0 && (it.status == "Sedang Diproses" || it.status == "Selesai")
                }.sortedBy { it.status == "Selesai" }
                if (isAdded) {
                    adapter.updateData(filteredCourses)
                }
            } finally {
                fetchProgressJob = null
                if (swipeRefreshLayout.isRefreshing) {
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        }
    }

    private suspend fun fetchLocalLessonProgress(updatedCourses: MutableList<Course>) {
        val lessonProgressList = withContext(Dispatchers.IO) {
            userRepository.getLessonProgressByUser(username)
        }
        lessonProgressList.forEach { progressItem ->
            val index = updatedCourses.indexOfFirst { it.lessonId == progressItem.lessonId }
            if (index != -1) {
                updatedCourses[index] = Course(
                    title = updatedCourses[index].title,
                    status = when (progressItem.status) {
                        "not_started" -> "Belum Mulai"
                        "in_progress" -> "Sedang Diproses"
                        "completed" -> "Selesai"
                        else -> "Belum Mulai"
                    },
                    progress = progressItem.progress,
                    iconResId = updatedCourses[index].iconResId,
                    lessonId = progressItem.lessonId
                )
            }
        }
    }

    override fun onDestroyView() {
        fetchProgressJob?.cancel()
        super.onDestroyView()
    }

    companion object {
        private const val TAG = "CourseFragment"
    }
}