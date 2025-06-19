package com.app.mytenses.Activity

import CourseRingkasanSimplePresentFragment
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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.mytenses.R
import com.app.mytenses.TenseCard
import com.app.mytenses.TenseCardAdapter
import com.app.mytenses.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private var selectedButton: Button? = null
    private lateinit var adapter: TenseCardAdapter
    private lateinit var username: String
    private val apiService = RetrofitClient.apiService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get username from SharedPreferences
        val sharedPreferences = requireActivity().getSharedPreferences("MyTensesPrefs", Context.MODE_PRIVATE)
        username = "user_${sharedPreferences.getInt("user_id", -1)}"
        val fullName = sharedPreferences.getString("name", null)
        Log.d(TAG, "SharedPreferences - name: $fullName, username: $username")

        // Welcome TextView setup
        val welcomeTextView = view.findViewById<TextView>(R.id.textView2)
        val firstName = fullName?.split(" ")?.firstOrNull() ?: "Pengguna"
        val welcomeText = getString(R.string.welcome_message, firstName)

        val spannable = SpannableString(welcomeText)
        val nameStartIndex = welcomeText.indexOf(firstName)
        val nameEndIndex = nameStartIndex + firstName.length

        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.blue)),
            nameStartIndex,
            nameEndIndex,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        welcomeTextView.text = spannable

        // RecyclerView setup with default cards
        val rvTenseCards = view.findViewById<RecyclerView>(R.id.rvTenseCards)
        val defaultTenseCards = listOf(
            TenseCard("Simple Present", "Belum Mulai", 0, R.drawable.simple_present, "simple_present"),
            TenseCard("Simple Past", "Belum Mulai", 0, R.drawable.simple_past, "simple_past"),
            TenseCard("Simple Future", "Belum Mulai", 0, R.drawable.simple_future, "simple_future"),
            TenseCard("Simple Past Future", "Belum Mulai", 0, R.drawable.simple_past_future, "simple_past_future")
        )
        adapter = TenseCardAdapter(defaultTenseCards) { tenseCard ->
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
        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return 1
            }
        }
        rvTenseCards.layoutManager = gridLayoutManager

        // Fetch progress for all courses
        fetchAllLessonProgress()

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

    private fun fetchAllLessonProgress() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.getLessonProgress(username)
                }
                if (response.isSuccessful) {
                    val lessonProgressList = response.body()?.lesson_progress ?: emptyList()
                    // Initialize default cards
                    val updatedCards = mutableListOf<TenseCard>().apply {
                        add(TenseCard("Simple Present", "Belum Mulai", 0, R.drawable.simple_present, "simple_present"))
                        add(TenseCard("Simple Past", "Belum Mulai", 0, R.drawable.simple_past, "simple_past"))
                        add(TenseCard("Simple Future", "Belum Mulai", 0, R.drawable.simple_future, "simple_future"))
                        add(TenseCard("Simple Past Future", "Belum Mulai", 0, R.drawable.simple_past_future, "simple_past_future"))
                    }
                    // Update cards with API data if available
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
                    adapter.updateData(updatedCards.sortedBy { it.title })
                } else {
                    Log.e(TAG, "Failed to fetch progress: ${response.message()}")
                    // Keep default cards if API call fails
                    adapter.updateData(listOf(
                        TenseCard("Simple Present", "Belum Mulai", 0, R.drawable.simple_present, "simple_present"),
                        TenseCard("Simple Past", "Belum Mulai", 0, R.drawable.simple_past, "simple_past"),
                        TenseCard("Simple Future", "Belum Mulai", 0, R.drawable.simple_future, "simple_future"),
                        TenseCard("Simple Past Future", "Belum Mulai", 0, R.drawable.simple_past_future, "simple_past_future")
                    ))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching progress: ${e.message}")
                // Keep default cards if exception occurs
                adapter.updateData(listOf(
                    TenseCard("Simple Present", "Belum Mulai", 0, R.drawable.simple_present, "simple_present"),
                    TenseCard("Simple Past", "Belum Mulai", 0, R.drawable.simple_past, "simple_past"),
                    TenseCard("Simple Future", "Belum Mulai", 0, R.drawable.simple_future, "simple_future"),
                    TenseCard("Simple Past Future", "Belum Mulai", 0, R.drawable.simple_past_future, "simple_past_future")
                ))
            }
        }
    }

    private fun updateRecyclerView(filter: String) {
        Log.d(TAG, "Selected filter: $filter")
        // Implement filtering logic if needed
    }

    companion object {
        private const val TAG = "HomeFragment"
    }
}