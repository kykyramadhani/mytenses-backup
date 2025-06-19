package com.app.mytenses.Activity

import CourseMateriSimplePresentFragment
import android.content.Context
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.app.mytenses.Activity.CompletedCourseFragment
import com.app.mytenses.R

class QuizResultFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_quiz_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve totalScore from arguments
        val totalScore = arguments?.getInt("totalScore") ?: 0

        // Display totalScore
        val scoreTextView = view.findViewById<TextView>(R.id.scoreText)
        scoreTextView.text = "$totalScore/100"

        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            val targetFragment = CourseMateriSimplePresentFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, targetFragment)
                .addToBackStack(null)
                .commit()
        }

        val congratsTextView = view.findViewById<TextView>(R.id.congratsText)
        val sharedPreferences = requireContext().getSharedPreferences("MyTensesPrefs", Context.MODE_PRIVATE)
        val fullName = sharedPreferences.getString("name", null)

        val firstName = fullName?.split(" ")?.firstOrNull() ?: "Pengguna"
        val welcomeText = getString(R.string.congrats_message, firstName)

        val spannable = SpannableString(welcomeText)
        val nameStartIndex = welcomeText.indexOf(firstName)
        val nameEndIndex = nameStartIndex + firstName.length

        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.blue)),
            nameStartIndex,
            nameEndIndex,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        congratsTextView.text = spannable


        val btnNext = view.findViewById<Button>(R.id.btnNextResult)
        btnNext.setOnClickListener {
            val lessonId = arguments?.getString("lessonId")
            val completedFragment = CompletedCourseFragment().apply {
                arguments = Bundle().apply {
                    putString("lessonId", lessonId)
                }
            }
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, completedFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    companion object {
        fun newInstance(totalScore: Int): QuizResultFragment {
            val fragment = QuizResultFragment()
            val args = Bundle()
            args.putInt("totalScore", totalScore)
            fragment.arguments = args
            return fragment
        }
    }
}