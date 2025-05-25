package com.app.mytenses.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.app.mytenses.R

class CourseMateriSimplePresentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_materi_simple_present)

        val tabRingkasan = findViewById<TextView>(R.id.tabRingkasan)

        tabRingkasan.setOnClickListener {
            val intent = Intent(this, CourseRingkasanSimplePresentActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}