package com.mobdeve.s15.group4.sealcoffee

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // REMOVED the automatic auth.currentUser check here so the activity
        // safely inflates its layout first without freezing on a blank screen.

        setContentView(R.layout.activity_login)

        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val signUpLink = findViewById<TextView>(R.id.signUpLink)

        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()

            emailInput.error = null
            passwordInput.error = null

            if (email.isEmpty() || password.isEmpty()) {
                if (email.isEmpty()) emailInput.error = getString(R.string.email_required)
                if (password.isEmpty()) passwordInput.error = getString(R.string.password_required)
                return@setOnClickListener
            }

            loginButton.isEnabled = false

            // Authenticate with Firebase Auth
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    if (authResult.user != null) {
                        // Let AuthNavigation handle role-checking via Firestore and routing
                        AuthNavigation.routeAuthenticated(this@LoginActivity)
                    } else {
                        loginButton.isEnabled = true
                        Toast.makeText(this, "Authentication failed: Missing User UID", Toast.LENGTH_LONG).show()
                    }
                }
                .addOnFailureListener { exception ->
                    loginButton.isEnabled = true
                    Toast.makeText(
                        this@LoginActivity,
                        "Login failed: ${exception.localizedMessage ?: getString(R.string.invalid_credentials)}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }

        signUpLink.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}