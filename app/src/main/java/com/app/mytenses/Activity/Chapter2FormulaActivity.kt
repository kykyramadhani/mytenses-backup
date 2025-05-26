package com.app.mytenses.Activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.app.mytenses.databinding.ActivityChapter2FormulaBinding
import com.app.mytenses.R

class Chapter2FormulaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChapter2FormulaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChapter2FormulaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        println("Chapter2Formula: onCreate called")

        // Load fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, Chapter2FormulaFragment.newInstance())
                .commit()
            println("Chapter2Formula: Fragment loaded")
        }

        // Set up navigation buttons
        binding.btnBackChapt2.setOnClickListener {
            println("Chapter2Formula: Back clicked")
            navigateTo(HomeFragment::class.java)
        }
        binding.btnNextChapt2.setOnClickListener {
            println("Chapter2Formula: Next clicked")
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
