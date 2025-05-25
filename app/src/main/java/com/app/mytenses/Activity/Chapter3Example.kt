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

class Chapter3Example : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chapter3_example)

        val prevBtn = findViewById<ImageButton>(R.id.btnBackChapt3)
        val nextBtn = findViewById<Button>(R.id.btnNextChapt3)

        prevBtn.setOnClickListener {
            navigateTo(OnBoarding2Activity::class.java)
        }


    }

    private fun navigateTo(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        startActivity(intent)
        finish()
    }
}