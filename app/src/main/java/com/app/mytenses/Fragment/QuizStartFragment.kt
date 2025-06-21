package com.app.mytenses.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import com.app.mytenses.R

class QuizStartFragment : Fragment() {

    private var lessonId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lessonId = arguments?.getString("lesson_id")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.activity_quiz_start, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBackQuiz = view.findViewById<ImageButton>(R.id.btnBackQuiz)
        val btnMulaiQuiz = view.findViewById<AppCompatButton>(R.id.btnMulaiQuiz)

        btnBackQuiz.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        btnMulaiQuiz.setOnClickListener {
            val quizFragment = QuizFragment().apply {
                arguments = Bundle().apply {
                    putString("lesson_id", lessonId)
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, quizFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    companion object {
        fun newInstance(lessonId: String): QuizStartFragment {
            val fragment = QuizStartFragment()
            fragment.arguments = Bundle().apply {
                putString("lesson_id", lessonId)
            }
            return fragment
        }
    }
}
