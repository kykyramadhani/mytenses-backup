package com.app.mytenses.Activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.app.mytenses.databinding.ActivityChapter1LessonBinding
import com.app.mytenses.R

class Chapter1LessonActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChapter1LessonBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChapter1LessonBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, Chapter1LessonFragment.newInstance())
                .commit()
        }

        binding.btnNextChapt1.setOnClickListener {
            navigateTo(Chapter2FormulaActivity::class.java)
        }
    }

    private fun navigateTo(activityClass: Class<*>) {
        try {
            startActivity(Intent(this, activityClass))
            finish()
        } catch (e: Exception) {
            println("Chapter1Lesson: Error navigating to $activityClass: $e")
        }
    }
}
