package com.app.mytenses.Fragment

import android.content.Context
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.mytenses.R
import com.app.mytenses.TenseCard
import com.app.mytenses.TenseCardAdapter
import com.app.mytenses.data.database.AppDatabase
import com.app.mytenses.data.repository.UserRepository
import com.app.mytenses.network.RetrofitClient
import com.app.mytenses.utils.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private var selectedButton: Button? = null
    private lateinit var adapter: TenseCardAdapter
    private lateinit var username: String
    private lateinit var userRepository: UserRepository
    private val apiService = RetrofitClient.apiService
    private var fetchProgressJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = view.context
        val activity = activity

        if (context == null || activity == null || !isAdded) {
            Log.w(TAG, "Fragment not attached to context, skipping initialization")
            return
        }

        // Inisialisasi UserRepository
        userRepository = UserRepository(apiService, context, AppDatabase.getDatabase(context).userDao())

        // Get username from SharedPreferences
        val sharedPreferences = activity.getSharedPreferences("MyTensesPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("user_id", -1)
        username = if (userId != -1) "user_$userId" else ""
        val fullName = sharedPreferences.getString("name", null)
        Log.d(TAG, "SharedPreferences - name: $fullName, username: $username")

        if (username.isEmpty()) {
            Log.w(TAG, "Invalid username, skipping further initialization")
            return
        }

        // Welcome TextView setup
        val welcomeTextView = view.findViewById<TextView>(R.id.textView2)
        val firstName = fullName?.split(" ")?.firstOrNull() ?: "Pengguna"
        val welcomeText = getString(R.string.welcome_message, firstName)

        val spannable = SpannableString(welcomeText)
        val nameStartIndex = welcomeText.indexOf(firstName)
        val nameEndIndex = nameStartIndex + firstName.length

        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(context, R.color.blue)),
            nameStartIndex,
            nameEndIndex,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        welcomeTextView.text = spannable

        // RecyclerView setup
        val rvTenseCards = view.findViewById<RecyclerView>(R.id.rvTenseCards)
        rvTenseCards.layoutManager = GridLayoutManager(context, 2).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int = 1
            }
        }

        // Inisialisasi adapter tanpa data awal
        adapter = TenseCardAdapter(mutableListOf()) { tenseCard ->
            val fragment = CourseRingkasanSimplePresentFragment().apply {
                arguments = Bundle().apply {
                    putString("TITLE", tenseCard.title)
                    putString("STATUS", tenseCard.status)
                    putInt("PROGRESS", tenseCard.progress)
                    putInt("IMAGE_RES_ID", tenseCard.imageResId)
                    putString("LESSON_ID", tenseCard.lessonId)
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        rvTenseCards.adapter = adapter

        // Tampilkan loading indicator (opsional, tambahkan di layout jika diperlukan)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        progressBar?.visibility = View.VISIBLE

        // Fetch data
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            fetchAllLessonProgress()
            progressBar?.visibility = View.GONE
        }

        // Button selection logic
        val buttons = listOf(
            view.findViewById<Button>(R.id.btnSimple),
            view.findViewById<Button>(R.id.btnContinuous),
            view.findViewById<Button>(R.id.btnPerfect),
            view.findViewById<Button>(R.id.btnPerfectContinuous),
            view.findViewById<Button>(R.id.btnPresent),
            view.findViewById<Button>(R.id.btnPast),
            view.findViewById<Button>(R.id.btnFuture),
            view.findViewById<Button>(R.id.btnPresentPerfect),
            view.findViewById<Button>(R.id.btnPastPerfect)
        )

        selectedButton = buttons[0]
        selectedButton?.isSelected = true

        buttons.forEach { button ->
            button.setOnClickListener {
                selectedButton?.isSelected = false
                button.isSelected = true
                selectedButton = button
                updateRecyclerView(button.text.toString())
            }
        }
    }

    private suspend fun fetchAllLessonProgress() {
        fetchProgressJob?.cancel()
        fetchProgressJob = viewLifecycleOwner.lifecycleScope.launch {
            val context = view?.context ?: return@launch
            val updatedCards = mutableListOf<TenseCard>().apply {
                add(TenseCard("Simple Present", "Belum Mulai", 0, R.drawable.simple_present, "simple_present"))
                add(TenseCard("Simple Past", "Belum Mulai", 0, R.drawable.simple_past, "simple_past"))
                add(TenseCard("Simple Future", "Belum Mulai", 0, R.drawable.simple_future, "simple_future"))
                add(TenseCard("Simple Past Future", "Belum Mulai", 0, R.drawable.simple_past_future, "simple_past_future"))
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
                            val index = updatedCards.indexOfFirst { it.lessonId == progressItem.lesson_id }
                            if (index != -1) {
                                updatedCards[index] = TenseCard(
                                    title = progressItem.title,
                                    status = when (progressItem.status) {
                                        "not_started" -> "Belum Mulai"
                                        "in_progress" -> "Sedang Diproses"
                                        "completed" -> "Selesai"
                                        else -> "Belum Mulai"
                                    },
                                    progress = progressItem.progress,
                                    imageResId = updatedCards[index].imageResId,
                                    lessonId = progressItem.lesson_id
                                )
                            }
                        }
                        // Sinkronkan data ke database
                        withContext(Dispatchers.IO) {
                            userRepository.syncUserData(username)
                        }
                    } else {
                        Log.e(TAG, "Failed to fetch progress from API: ${response.message()}")
                        fetchLocalLessonProgress(updatedCards)
                    }
                } else {
                    Log.d(TAG, "Offline: Fetching lesson progress from database")
                    fetchLocalLessonProgress(updatedCards)
                }
                // Urutkan: Belum Mulai/In Progress di atas, Selesai di bawah
                val sortedCards = updatedCards.sortedBy { it.status == "Selesai" }
                adapter.updateData(sortedCards)
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) {
                    Log.d(TAG, "Fetch lesson progress cancelled")
                    throw e
                }
                Log.e(TAG, "Error fetching progress: ${e.message}")
                val sortedCards = updatedCards.sortedBy { it.status == "Selesai" }
                adapter.updateData(sortedCards)
            }
        }
    }

    private suspend fun fetchLocalLessonProgress(updatedCards: MutableList<TenseCard>) {
        val lessonProgressList = withContext(Dispatchers.IO) {
            userRepository.getLessonProgressByUser(username)
        }
        lessonProgressList.forEach { progressItem ->
            val index = updatedCards.indexOfFirst { it.lessonId == progressItem.lessonId }
            if (index != -1) {
                updatedCards[index] = TenseCard(
                    title = updatedCards[index].title,
                    status = when (progressItem.status) {
                        "not_started" -> "Belum Mulai"
                        "in_progress" -> "Sedang Diproses"
                        "completed" -> "Selesai"
                        else -> "Belum Mulai"
                    },
                    progress = progressItem.progress,
                    imageResId = updatedCards[index].imageResId,
                    lessonId = progressItem.lessonId
                )
            }
        }
    }

    private fun updateRecyclerView(filter: String) {
        Log.d(TAG, "Selected filter: $filter")
        // Implement filtering logic if needed
    }

    override fun onDestroyView() {
        fetchProgressJob?.cancel()
        super.onDestroyView()
    }

    companion object {
        private const val TAG = "HomeFragment"
    }
}