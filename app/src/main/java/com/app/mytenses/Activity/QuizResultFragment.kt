package com.app.mytenses.Activity

import CourseMateriSimplePresentFragment
import android.content.Context
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.app.mytenses.R

class QuizResultFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_quiz_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup tombol kembali
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            // Navigasi eksplisit ke CourseMateriSimplePresentFragment
            val targetFragment = CourseMateriSimplePresentFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, targetFragment)
                .addToBackStack(null)
                .commit()
        }

        // Setup teks ucapan dengan nama pengguna
        val congratsTextView = view.findViewById<TextView>(R.id.congratsText)
        val sharedPreferences = requireContext().getSharedPreferences("MyTensesPrefs", Context.MODE_PRIVATE)
        val fullName = sharedPreferences.getString("name", null)

        // Ambil nama depan atau gunakan "Pengguna" jika null
        val firstName = fullName?.split(" ")?.firstOrNull() ?: "Pengguna"
        val welcomeText = getString(R.string.congrats_message, firstName) // Misal: "Selamat %s !"

        // Buat SpannableString untuk mewarnai nama
        val spannable = SpannableString(welcomeText)
        val nameStartIndex = welcomeText.indexOf(firstName)
        val nameEndIndex = nameStartIndex + firstName.length

        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.blue)),
            nameStartIndex,
            nameEndIndex,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        congratsTextView.text = spannable

        // Setup tombol Berikutnya
        val btnNext = view.findViewById<Button>(R.id.btnNextResult)
        btnNext.setOnClickListener {
            // Contoh navigasi ke fragment lain (sesuaikan dengan kebutuhan)
            val targetFragment = CourseMateriSimplePresentFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, targetFragment)
                .addToBackStack(null)
                .commit()
            // Atau pop back stack jika ingin kembali ke fragment sebelumnya
            // requireActivity().supportFragmentManager.popBackStack()
            // Atau selesaikan activity jika di akhir alur
            // requireActivity().finish()
        }
    }

    companion object {
        fun newInstance(): QuizResultFragment {
            return QuizResultFragment()
        }
    }
}