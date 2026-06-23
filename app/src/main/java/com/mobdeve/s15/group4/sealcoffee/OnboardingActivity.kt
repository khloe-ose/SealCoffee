package com.mobdeve.s15.group4.sealcoffee

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class OnboardingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        findViewById<Button>(R.id.getStartedButton).setOnClickListener {
            openLogin()
        }
        findViewById<View>(R.id.onboardingRoot).setOnClickListener {
            openLogin()
        }
        findViewById<View>(R.id.onboardingContent).setOnClickListener {
            openLogin()
        }
        findViewById<View>(R.id.logoBadge).setOnClickListener {
            openLogin()
        }
    }

    private fun openLogin() {
        Toast.makeText(this, "Opening login", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, LoginActivity::class.java))
    }
}
