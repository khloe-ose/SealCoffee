package com.mobdeve.s15.group4.sealcoffee

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class OnboardingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        val getStarted = findViewById<Button>(R.id.getStartedButton)
        getStarted.setOnClickListener { openLogin() }
        if (sealApp.session.hasSession) {
            getStarted.isEnabled = false
            lifecycleScope.launch {
                val user = sealApp.repository.getUser(sealApp.session.userId)
                if (user != null && user.role == sealApp.session.role?.name) {
                    AuthNavigation.routeAuthenticated(this@OnboardingActivity)
                } else {
                    sealApp.session.clear()
                    getStarted.isEnabled = true
                }
            }
        }
    }

    private fun openLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
    }
}
