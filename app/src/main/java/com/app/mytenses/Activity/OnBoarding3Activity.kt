package com.app.mytenses.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.app.mytenses.R

class OnBoarding3Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_on_boarding3)

        val btnNext = findViewById<Button>(R.id.btnNextBoarding3)
        val btnSkip = findViewById<TextView>(R.id.btnSkip)

        btnNext.setOnClickListener {
            val intent = Intent(this, OnBoarding5Activity::class.java)
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