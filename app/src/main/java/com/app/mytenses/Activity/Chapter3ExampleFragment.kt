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
        println("Chapter3ExampleFragment: onCreateView called")
        return inflater.inflate(R.layout.fragment_chapter3_example, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("Chapter3ExampleFragment: onViewCreated called")

        // Find views
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        println("btnBack: $btnBack")

        // Set back button listener
        btnBack?.setOnClickListener {
            println("Chapter3ExampleFragment: Back clicked")
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Next button is disabled in XML, no action needed
        println("Chapter3ExampleFragment: Next button disabled")
    }

    companion object {
        @JvmStatic
        fun newInstance() = Chapter3ExampleFragment()
    }
}
