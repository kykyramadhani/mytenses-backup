package com.app.mytenses.Activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.app.mytenses.databinding.ActivityChapter2FormulaBinding
import com.app.mytenses.R
import CourseMateriSimplePresent

class Chapter2FormulaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChapter2FormulaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChapter2FormulaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, Chapter2FormulaFragment.newInstance())
                .commit()
        }

        // Set up navigation buttons
        binding.btnBackChapt2.setOnClickListener {
            navigateTo(CourseMateriSimplePresent::class.java)
        }
        binding.btnNextChapt2.setOnClickListener {
            navigateTo(Chapter3Example::class.java)
        }
    }

    private fun navigateTo(activityClass: Class<*>) {
        try {
            startActivity(Intent(this, activityClass))
            finish()
        } catch (e: Exception) {
            println("Chapter2Formula: Error navigating to $activityClass: $e")
        }
    }
}
