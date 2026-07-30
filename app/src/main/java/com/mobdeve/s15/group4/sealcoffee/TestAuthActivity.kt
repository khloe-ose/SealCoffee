package com.mobdeve.s15.group4.sealcoffee

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.AuthResult


class TestAuthActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_auth)

        auth = FirebaseAuth.getInstance()

        val emailInput = findViewById<EditText>(R.id.testEmailInput)
        val passwordInput = findViewById<EditText>(R.id.testPasswordInput)
        val loginButton = findViewById<Button>(R.id.testLoginButton)
        val logoutButton = findViewById<Button>(R.id.testLogoutButton)
        val statusText = findViewById<TextView>(R.id.testStatusText)

        // Update initial status based on current session
        updateLoginStatus(statusText)

        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                statusText.text = "Error: Please fill in email and password."
                return@setOnClickListener
            }

            statusText.text = "Signing in..."
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    statusText.text = "Success! Logged in as: ${result.user?.email}"
                }
                .addOnFailureListener { exception ->
                    statusText.text = "Failed: ${exception.localizedMessage}"
                }
        }

        logoutButton.setOnClickListener {
            auth.signOut()
            updateLoginStatus(statusText)
            statusText.text = "Status: Signed out successfully."
        }
    }

    private fun updateLoginStatus(statusText: TextView) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            statusText.text = "Status: Logged in (${currentUser.email})"
        } else {
            statusText.text = "Status: Not Logged In"
        }
    }
}