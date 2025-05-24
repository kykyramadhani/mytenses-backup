package com.app.mytenses.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.app.mytenses.R

class OnBoarding5Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_on_boarding5)

        val loginBtn = findViewById<Button>(R.id.loginButton)
        val signUpBtn = findViewById<Button>(R.id.registerButton)

        loginBtn.setOnClickListener {
            navigateTo(LoginActivity::class.java)
        }
        signUpBtn.setOnClickListener {
            navigateTo(SignUpActivity::class.java)
        }

    }

    private fun navigateTo(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        startActivity(intent)
        finish()
    }
}