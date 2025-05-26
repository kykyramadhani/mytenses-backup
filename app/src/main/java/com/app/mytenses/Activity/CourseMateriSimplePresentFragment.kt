package com.app.mytenses.Activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.app.mytenses.R

class CourseMateriSimplePresent : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        println("CourseMateriSimplePresent: onCreateView called")
        return inflater.inflate(R.layout.fragment_course_materi_simple_present, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("CourseMateriSimplePresent: onViewCreated called")

        // Back button
        val backButton = view.findViewById<ImageButton>(R.id.backButtonMateriSimplePresent)
        println("backButton: $backButton")
        backButton?.setOnClickListener {
            println("Back button clicked")
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Tab Ringkasan
        val tabRingkasan = view.findViewById<TextView>(R.id.tabRingkasan)
        println("tabRingkasan: $tabRingkasan")
        tabRingkasan?.setOnClickListener {
            println("Tab Ringkasan clicked")
            try {
                val fragment = CourseRingkasanSimplePresent()
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            } catch (e: Exception) {
                println("Error navigating to Ringkasan: $e")
            }
        }

        // Chapter buttons
        val btnChapter1 = view.findViewById<Button>(R.id.btnChapter1)
        println("btnChapter1: $btnChapter1")
        btnChapter1?.setOnClickListener {
            println("Chapter 1 clicked")
            try {
                startActivity(Intent(requireContext(), Chapter1LessonActivity::class.java))
            } catch (e: Exception) {
                println("Error starting Chapter1Lesson: $e")
            }
        }

        val btnChapter2 = view.findViewById<Button>(R.id.btnChapter2)
        println("btnChapter2: $btnChapter2")
        btnChapter2?.setOnClickListener {
            println("Chapter 2 clicked")
            try {
                startActivity(Intent(requireContext(), Chapter2FormulaActivity::class.java))
            } catch (e: Exception) {
                println("Error starting Chapter2Formula: $e")
            }
        }

        val btnChapter3 = view.findViewById<Button>(R.id.btnChapter3)
        println("btnChapter3: $btnChapter3")
        btnChapter3?.setOnClickListener {
            println("Chapter 3 clicked")
            try {
                startActivity(Intent(requireContext(), Chapter3Example::class.java))
            } catch (e: Exception) {
                println("Error starting Chapter3Example: $e")
            }
        }

        val btnChapter4 = view.findViewById<Button>(R.id.btnChapter4)
        println("btnChapter4: $btnChapter4")
        btnChapter4?.setOnClickListener {
            println("Chapter 4 clicked")
            // TODO: Implement QuizActivity
        }

        // BELAJAR button
        val btnBelajar = view.findViewById<Button>(R.id.btnBelajar)
        println("btnBelajar: $btnBelajar")
        btnBelajar?.setOnClickListener {
            println("Belajar clicked")
            try {
                startActivity(Intent(requireContext(), Chapter1LessonActivity::class.java))
            } catch (e: Exception) {
                println("Error starting Belajar: $e")
            }
        }
    }
}
