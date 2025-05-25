package com.app.mytenses.Activity

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
import android.widget.TextView

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        val rvTenseCards = view.findViewById<RecyclerView>(R.id.rvTenseCards)
        val tenseCards = listOf(
            TenseCard("Simple Present", "Sedang Diproses", 50, R.drawable.simple_present),
            TenseCard("Simple Past", "Belum Mulai", 0, R.drawable.simple_past),
            TenseCard("Simple Future", "Selesai", 100, R.drawable.simple_future),
            TenseCard("Simple Past Future", "Selesai", 100, R.drawable.simple_past_future)
        )

        val adapter = TenseCardAdapter(tenseCards)
        rvTenseCards.adapter = adapter

        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return 1
            }
        }
        rvTenseCards.layoutManager = gridLayoutManager
    }

    companion object {
        private val TAG = "HomeFragment"
    }
}