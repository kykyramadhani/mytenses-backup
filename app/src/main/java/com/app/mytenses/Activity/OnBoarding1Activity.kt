package com.app.mytenses.Activity

import android.os.Bundle
import android.content.Intent
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.app.mytenses.R

class OnBoarding1Activity : AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_on_boarding1)

            val btnNext = findViewById<Button>(R.id.btnNextBoarding1)
            val btnSkip = findViewById<TextView>(R.id.btnSkip)

            btnNext.setOnClickListener {
                val intent = Intent(this, OnBoarding2Activity::class.java)
                startActivity(intent)
                finish()
            }

            btnSkip.setOnClickListener {
                val intent = Intent(this, OnBoarding5Activity::class.java)
                startActivity(intent)
                finish()
            }


        }
}