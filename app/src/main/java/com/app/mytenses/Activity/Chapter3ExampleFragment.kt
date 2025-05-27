package com.app.mytenses.Activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.app.mytenses.R

class Chapter3ExampleFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chapter3_example, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find views
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)

        // Set back button listener
        btnBack?.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

    }

    companion object {
        @JvmStatic
        fun newInstance() = Chapter3ExampleFragment()
    }
}
