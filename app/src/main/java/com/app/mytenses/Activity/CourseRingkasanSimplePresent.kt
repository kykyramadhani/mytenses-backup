package com.app.mytenses.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.app.mytenses.R

class CourseRingkasanSimplePresentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_ringkasan_simple_present)

        val tabMateri = findViewById<TextView>(R.id.tabMateri)

        tabMateri.setOnClickListener {
            val intent = Intent(this, CourseMateriSimplePresentActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}