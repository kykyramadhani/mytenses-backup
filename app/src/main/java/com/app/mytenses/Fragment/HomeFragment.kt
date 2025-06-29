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
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
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
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var currentFilter: String? = null
    private val tenseCards = mutableListOf<TenseCard>().apply {
        add(TenseCard("Simple Present", "Belum Mulai", 0, R.drawable.simple_present, "simple_present"))
        add(TenseCard("Simple Past", "Belum Mulai", 0, R.drawable.simple_past, "simple_past"))
        add(TenseCard("Simple Future", "Belum Mulai", 0, R.drawable.simple_future, "simple_future"))
        add(TenseCard("Simple Past Future", "Belum Mulai", 0, R.drawable.simple_past_future, "simple_past_future"))
        add(TenseCard("Present Continuous", "Belum Mulai", 0, R.drawable.continuous_present, "present_continuous"))
        add(TenseCard("Past Continuous", "Belum Mulai", 0, R.drawable.continuous_past, "past_continuous"))
        add(TenseCard("Future Continuous", "Belum Mulai", 0, R.drawable.continuous_future, "future_continuous"))
        add(TenseCard("Past Future Continuous", "Belum Mulai", 0, R.drawable.continuous_past_future, "past_future_continuous"))
        add(TenseCard("Present Perfect", "Belum Mulai", 0, R.drawable.perfect_present, "present_perfect"))
        add(TenseCard("Past Perfect", "Belum Mulai", 0, R.drawable.perfect_past, "past_perfect"))
        add(TenseCard("Future Perfect", "Belum Mulai", 0, R.drawable.perfect_future, "future_perfect"))
        add(TenseCard("Past Future Perfect", "Belum Mulai", 0, R.drawable.perfect_past_future, "past_future_perfect"))
        add(TenseCard("Past Perfect Continuous", "Belum Mulai", 0, R.drawable.perfect_continuous_past, "past_perfect_continuous"))
        add(TenseCard("Future Perfect Continuous", "Belum Mulai", 0, R.drawable.perfect_continuous_future, "future_perfect_continuous"))
        add(TenseCard("Past Future Perfect Continuous", "Belum Mulai", 0, R.drawable.perfect_continuous_past_future, "past_future_perfect_continuous"))
    }

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

        currentFilter = savedInstanceState?.getString("currentFilter") ?: currentFilter

        userRepository = UserRepository(apiService, context, AppDatabase.getDatabase(context).userDao())

        val sharedPreferences = activity.getSharedPreferences("MyTensesPrefs", Context.MODE_PRIVATE)
        username = sharedPreferences.getString("username",null).toString()
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
        swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout) ?: run {
            Log.e(TAG, "swipeRefreshLayout not found in layout")
            return
        }
        rvTenseCards.layoutManager = GridLayoutManager(context, 2).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int = 1
            }
        }

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

        viewLifecycleOwner.lifecycleScope.launch {
            fetchLocalLessonProgress()
            updateRecyclerView(currentFilter ?: "")
        }

        swipeRefreshLayout.setOnRefreshListener {
            viewLifecycleOwner.lifecycleScope.launch {
                fetchAllLessonProgress()
                swipeRefreshLayout.isRefreshing = false
            }
        }

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

        // Ngatur tombol yang dipilih berdasarkan currentFilter gesss
        selectedButton = if (currentFilter != null) {
            buttons.find { it?.text?.toString()?.lowercase() == currentFilter?.lowercase() } ?: buttons.find { it != null }
        } else {
            buttons.find { it != null }
        }
        selectedButton?.isSelected = true
        currentFilter = currentFilter ?: selectedButton?.text?.toString()

        buttons.forEach { button ->
            if (button != null) {
                button.setOnClickListener {
                    selectedButton?.isSelected = false
                    button.isSelected = true
                    selectedButton = button
                    currentFilter = button.text.toString()
                    updateRecyclerView(currentFilter ?: "")
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("currentFilter", currentFilter)
    }

    private suspend fun fetchAllLessonProgress() {
        fetchProgressJob?.cancel()
        fetchProgressJob = viewLifecycleOwner.lifecycleScope.launch {
            val context = view?.context ?: return@launch
            try {
                if (NetworkUtils.isOnline(context)) {
                    Log.d(TAG, "Online: Fetching lesson progress from API")
                    val response = withContext(Dispatchers.IO) {
                        apiService.getLessonProgress(username)
                    }
                    if (response.isSuccessful) {
                        val lessonProgressList = response.body()?.lesson_progress ?: emptyList()
                        lessonProgressList.forEach { progressItem ->
                            val index = tenseCards.indexOfFirst { it.lessonId == progressItem.lesson_id }
                            if (index != -1) {
                                tenseCards[index] = TenseCard(
                                    title = progressItem.title,
                                    status = when (progressItem.status) {
                                        "not_started" -> "Belum Mulai"
                                        "in_progress" -> "Sedang Diproses"
                                        "completed" -> "Selesai"
                                        else -> "Belum Mulai"
                                    },
                                    progress = progressItem.progress,
                                    imageResId = tenseCards[index].imageResId,
                                    lessonId = progressItem.lesson_id
                                )
                            }
                        }
                        withContext(Dispatchers.IO) {
                            userRepository.syncUserData(username)
                        }
                    } else {
                        Log.e(TAG, "Failed to fetch progress from API: ${response.code()} - ${response.message()}")
                    }
                }
                fetchLocalLessonProgress()
                if (isAdded) {
                    updateRecyclerView(currentFilter ?: "")
                }
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    Log.e(TAG, "Error fetching progress: ${e.message}", e)
                }
                fetchLocalLessonProgress()
                if (isAdded) {
                    updateRecyclerView(currentFilter ?: "")
                }
            } finally {
                fetchProgressJob = null
                if (swipeRefreshLayout.isRefreshing) {
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        }
    }

    private suspend fun fetchLocalLessonProgress() {
        val lessonProgressList = withContext(Dispatchers.IO) {
            userRepository.getLessonProgressByUser(username)
        }
        lessonProgressList.forEach { progressItem ->
            val index = tenseCards.indexOfFirst { it.lessonId == progressItem.lessonId }
            if (index != -1) {
                tenseCards[index] = TenseCard(
                    title = tenseCards[index].title,
                    status = when (progressItem.status) {
                        "not_started" -> "Belum Mulai"
                        "in_progress" -> "Sedang Diproses"
                        "completed" -> "Selesai"
                        else -> "Belum Mulai"
                    },
                    progress = progressItem.progress,
                    imageResId = tenseCards[index].imageResId,
                    lessonId = progressItem.lessonId
                )
            }
        }
    }

    private fun updateRecyclerView(filter: String) {
        Log.d(TAG, "Selected filter: $filter")
        viewLifecycleOwner.lifecycleScope.launch {
            val filteredCards = when (filter.lowercase()) {
                "simple" -> tenseCards.filter { it.title.contains("Simple", ignoreCase = true) }
                "continuous" -> tenseCards.filter { it.title.contains("Continuous", ignoreCase = true) }
                "perfect" -> tenseCards.filter { it.title.contains("Perfect", ignoreCase = true) && !it.title.contains("Perfect Continuous", ignoreCase = true) }
                "perfect continuous" -> tenseCards.filter { it.title.contains("Perfect Continuous", ignoreCase = true) }
                "present" -> tenseCards.filter { it.title.contains("Present", ignoreCase = true) }
                "past" -> tenseCards.filter { it.title.contains("Past", ignoreCase = true) }
                "future" -> tenseCards.filter { it.title.contains("Future", ignoreCase = true) }
                "present perfect" -> tenseCards.filter { it.title == "Present Perfect" }
                "past perfect" -> tenseCards.filter { it.title == "Past Perfect" }
                else -> tenseCards
            }
            val sortedCards = filteredCards.sortedBy { it.status == "Selesai" }
            adapter.updateData(sortedCards)
        }
    }

    override fun onDestroyView() {
        fetchProgressJob?.cancel()
        super.onDestroyView()
    }

    companion object {
        private const val TAG = "HomeFragment"
    }
}