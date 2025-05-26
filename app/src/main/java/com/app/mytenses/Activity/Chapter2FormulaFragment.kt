package com.app.mytenses.Activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.mytenses.R

class Chapter2FormulaFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        println("Chapter2FormulaFragment: onCreateView called")
        return inflater.inflate(R.layout.fragment_chapter2_formula, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance() = Chapter2FormulaFragment()
    }
}
