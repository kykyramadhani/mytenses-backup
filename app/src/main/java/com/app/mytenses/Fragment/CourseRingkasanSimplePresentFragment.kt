package com.app.mytenses.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.app.mytenses.R

class CourseRingkasanSimplePresentFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_course_ringkasan_simple_present, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // Tombol kembali: skip CourseMateri jika ada di backstack
        view.findViewById<ImageButton>(R.id.backButton)?.setOnClickListener {
            val fm = requireActivity().supportFragmentManager
            val count = fm.backStackEntryCount

            for (i in count - 1 downTo 0) {
                val entry = fm.getBackStackEntryAt(i)
                if (entry.name == "CourseMateri") {
                    fm.popBackStack(entry.name, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                    break
                }
            }

            // Pop ke fragment sebelumnya
            fm.popBackStack()
        }

        // Tab Materi -> Ganti fragment ke CourseMateriSimplePresent
        val tabMateri = view.findViewById<TextView>(R.id.tabMateri)
        tabMateri.setOnClickListener {
            val fragment = CourseMateriSimplePresentFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
//                .addToBackStack("CourseRingkasan")
                .commit()
        }

        // Tombol BELAJAR
        val btnMulaiBelajar = view.findViewById<Button>(R.id.btnBelajar)
        btnMulaiBelajar.setOnClickListener {
            val fragment = Chapter1LessonFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }
}
