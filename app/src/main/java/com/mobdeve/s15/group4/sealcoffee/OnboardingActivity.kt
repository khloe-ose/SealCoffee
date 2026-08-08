package com.mobdeve.s15.group4.sealcoffee

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class OnboardingActivity : AppCompatActivity() {

    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirestoreUserSeedUtility.seedUsersIfNeeded()
        FirestoreSeedUtility.seedInfoIfNeeded()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            AuthNavigation.routeAuthenticated(this)
            return
        }

        setContentView(R.layout.activity_onboarding)

        val getStarted = findViewById<Button>(R.id.getStartedButton)
        getStarted.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}