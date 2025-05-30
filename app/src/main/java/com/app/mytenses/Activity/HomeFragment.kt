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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.mytenses.R
import com.app.mytenses.TenseCard
import com.app.mytenses.TenseCardAdapter
import android.widget.Button
import android.widget.TextView

class HomeFragment : Fragment() {

    private var selectedButton: Button? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Welcome TextView setup
        val welcomeTextView = view.findViewById<TextView>(R.id.textView2)
        val sharedPreferences = requireActivity().getSharedPreferences("MyTensesPrefs", Context.MODE_PRIVATE)
        val fullName = sharedPreferences.getString("name", null)
        Log.d(TAG, "SharedPreferences - name: $fullName")

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

        // RecyclerView setup
        val rvTenseCards = view.findViewById<RecyclerView>(R.id.rvTenseCards)
        val tenseCards = listOf(
            TenseCard("Simple Present", "Sedang Diproses", 50, R.drawable.simple_present),
            TenseCard("Simple Past", "Belum Mulai", 0, R.drawable.simple_past),
            TenseCard("Simple Future", "Selesai", 100, R.drawable.simple_future),
            TenseCard("Simple Past Future", "Selesai", 100, R.drawable.simple_past_future)
        )
        val adapter = TenseCardAdapter(tenseCards) { tenseCard ->
            // Navigasi ke CourseRingkasanSimplePresentFragment saat item diklik
            val fragment = CourseRingkasanSimplePresentFragment().apply {
                arguments = Bundle().apply {
                    putString("TITLE", tenseCard.title)
                    putString("STATUS", tenseCard.status)
                    putInt("PROGRESS", tenseCard.progress)
                    putInt("IMAGE_RES_ID", tenseCard.imageResId)
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

        // Set default selected button (e.g., btnSimple)
        selectedButton = buttons[0]
        selectedButton?.isSelected = true

        buttons.forEach { button ->
            button.setOnClickListener {
                // Deselect previous button
                selectedButton?.isSelected = false
                // Select clicked button
                button.isSelected = true
                selectedButton = button
                // Optionally, update RecyclerView based on selected button
                updateRecyclerView(button.text.toString())
            }
        }
    }

    private fun updateRecyclerView(filter: String) {
        // Implement logic to filter RecyclerView based on the selected button
        Log.d(TAG, "Selected filter: $filter")
        // Update your RecyclerView adapter here based on the filter
    }

    companion object {
        private const val TAG = "HomeFragment"
    }
}