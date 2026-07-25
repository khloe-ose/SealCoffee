package com.mobdeve.s15.group4.sealcoffee

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (sealApp.session.hasSession) {
            AuthNavigation.routeAuthenticated(this)
            return
        }
        setContentView(R.layout.activity_login)

        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val loginButton = findViewById<Button>(R.id.loginButton)

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
            lifecycleScope.launch {
                val authentication = runCatching {
                    sealApp.repository.authenticate(email, password)
                }
                if (authentication.isFailure) {
                    Toast.makeText(
                        this@LoginActivity,
                        R.string.login_temporarily_unavailable,
                        Toast.LENGTH_LONG
                    ).show()
                    loginButton.isEnabled = true
                    return@launch
                }
                val user = authentication.getOrNull()
                if (user == null) {
                    Toast.makeText(
                        this@LoginActivity,
                        R.string.invalid_credentials,
                        Toast.LENGTH_SHORT
                    ).show()
                    loginButton.isEnabled = true
                } else {
                    sealApp.session.save(user)
                    AuthNavigation.routeAuthenticated(this@LoginActivity)
                }
            }
        }
        findViewById<TextView>(R.id.signUpLink).setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}
