package com.app.mytenses.Activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.app.mytenses.databinding.ActivityChapter3ExampleBinding
import com.app.mytenses.R

class Chapter3Example : AppCompatActivity() {
    private lateinit var binding: ActivityChapter3ExampleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChapter3ExampleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        println("Chapter3Example: onCreate called")

        // Load fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, Chapter3ExampleFragment.newInstance())
                .commit()
            println("Chapter3Example: Fragment loaded")
        }

        // Set up navigation buttons
        binding.btnBackChapt3.setOnClickListener {
            println("Chapter3Example: Back clicked")
            navigateTo(CourseMateriSimplePresent::class.java)
        }
        binding.btnNextChapt3.isEnabled = false
        println("Chapter3Example: Next button disabled")
    }

    private fun navigateTo(activityClass: Class<*>) {
        try {
            startActivity(Intent(this, activityClass))
            finish()
        } catch (e: Exception) {
            println("Chapter3Example: Error navigating to $activityClass: $e")
        }
    }
}
