package com.app.mytenses.Activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import com.app.mytenses.R

class Chapter2FormulaFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        println("Chapter2FormulaFragment: onCreateView called")
        return inflater.inflate(R.layout.fragment_chapter2_formula, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find views
        val btnBack = view.findViewById<ImageButton>(R.id.btnBackChapt2)
        val btnNext = view.findViewById<Button>(R.id.btnNextChapt2)

        // Set back button listener
        btnBack?.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Set next button listener
        btnNext?.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, Chapter3ExampleFragment.newInstance())
                .addToBackStack(null)
                .commit()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = Chapter2FormulaFragment()
    }
}
