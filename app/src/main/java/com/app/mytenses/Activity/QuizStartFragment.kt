package com.app.mytenses.Activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import com.app.mytenses.R

class QuizStartFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_quiz_start, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find views
        val btnBackQuiz = view.findViewById<ImageButton>(R.id.btnBackQuiz)
        val btnBerikutnyaQuiz = view.findViewById<AppCompatButton>(R.id.btnMulaiQuiz)

        // Set arrow back button listener to use popBackStack
        btnBackQuiz?.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Set "Berikutnya" button listener (optional, to be implemented if needed)
        btnBerikutnyaQuiz?.setOnClickListener {
            // Example:
            // requireActivity().supportFragmentManager.beginTransaction()
            //     .replace(R.id.fragment_container, QuizFragment.newInstance())
            //     .addToBackStack(null)
            //     .commit()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = QuizStartFragment()
    }
}