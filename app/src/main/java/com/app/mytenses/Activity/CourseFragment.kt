package com.app.mytenses.Activity

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.mytenses.R
import com.app.mytenses.CourseAdapter
import com.app.mytenses.Course

class CourseFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_course, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi RecyclerView menggunakan view yang sudah diinflasi
        val rvCourses = view.findViewById<RecyclerView>(R.id.rvCourses)
        if (rvCourses != null) {
            rvCourses.layoutManager = LinearLayoutManager(requireContext())

            // Data dummy untuk kursus
            val courseList = listOf(
                Course("Simple Present", "Selesai", 100, R.drawable.simple_present),
                Course("Simple Past", "Sedang Dipelajari", 50, R.drawable.simple_past),
                Course("Simple Future", "Sedang Dipelajari", 30, R.drawable.simple_future),
                Course("Simple Past Future", "Sedang Dipelajari", 20, R.drawable.simple_present),
                Course("Continuous Present", "Sedang Dipelajari", 10, R.drawable.continuous_present)
            )

            // Debugging: Cek jumlah item
            Log.d("CourseActivity", "Jumlah kursus: ${courseList.size}")

            // Set adapter ke RecyclerView
            val adapter = CourseAdapter(courseList)
            rvCourses.adapter = adapter
        }
    }
}