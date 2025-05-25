package com.app.mytenses.Activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        return inflater.inflate(R.layout.fragment_course_materi_simple_present, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Tombol kembali
        view.findViewById<ImageButton>(R.id.backButtonMateriSimplePresent)?.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Tab Ringkasan -> Ganti fragment kembali
        val tabRingkasan = view.findViewById<TextView>(R.id.tabRingkasan)
        tabRingkasan.setOnClickListener {
            val fragment = CourseRingkasanSimplePresent()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }
}

