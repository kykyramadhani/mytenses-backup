package com.app.mytenses.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.app.mytenses.R

class Chapter1Lesson : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chapter1_lesson)

        val prevBtn = findViewById<ImageButton>(R.id.btnBackChapt1)
        val nextBtn = findViewById<Button>(R.id.btnNextChapt1)

        prevBtn.setOnClickListener {
            navigateTo(OnBoarding5Activity::class.java)
        }
        nextBtn.setOnClickListener {
            navigateTo(Chapter2Formula::class.java)
        }

    }

    private fun navigateTo(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        startActivity(intent)
        finish()
    }
}