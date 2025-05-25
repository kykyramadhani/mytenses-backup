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

class Chapter2Formula : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chapter2_formula)

        val prevBtn = findViewById<ImageButton>(R.id.btnBackChapt2)
        val nextBtn = findViewById<Button>(R.id.btnNextChapt2)

        prevBtn.setOnClickListener {
            navigateTo(OnBoarding1Activity::class.java)
        }
        nextBtn.setOnClickListener {
            navigateTo(Chapter3Example::class.java)
        }

    }

    private fun navigateTo(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        startActivity(intent)
        finish()
    }
}