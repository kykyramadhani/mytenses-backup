package com.app.mytenses.Activity

import CourseRingkasanSimplePresentFragment
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.mytenses.R
import com.app.mytenses.CourseAdapter
import com.app.mytenses.Course

class CourseFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_course, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvCourses = view.findViewById<RecyclerView>(R.id.rvCourses)
        if (rvCourses != null) {
            rvCourses.layoutManager = LinearLayoutManager(view.context)

            val courseList = listOf(
                Course("Simple Present", "Selesai", 100, R.drawable.simple_present),
                Course("Simple Past", "Sedang Dipelajari", 50, R.drawable.simple_past),
                Course("Simple Future", "Sedang Dipelajari", 30, R.drawable.simple_future),
                Course("Simple Past Future", "Sedang Dipelajari", 20, R.drawable.simple_present),
                Course("Continuous Present", "Sedang Dipelajari", 10, R.drawable.continuous_present)
            )

            Log.d("CourseFragment", "Jumlah kursus: ${courseList.size}")

            val adapter = CourseAdapter(courseList)
            rvCourses.adapter = adapter

            adapter.setOnItemClickListener(object : CourseAdapter.OnItemClickListener {
                override fun onItemClick(position: Int) {
                    val course = courseList[position]
                    if (course.title == "Simple Present") {
                        val fragment = CourseRingkasanSimplePresentFragment()
                        val transaction: FragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
                        transaction.replace(R.id.fragment_container, fragment)
                        transaction.addToBackStack(null)
                        transaction.commit()
                    } else {
                        Log.w("CourseFragment", "Ringkasan hanya tersedia untuk Simple Present saat ini")
                    }
                }
            })
        } else {
            Log.e("CourseFragment", "rvCourses is null, check layout file!")
        }
    }
}