package com.mobdeve.s15.group4.sealcoffee

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mobdeve.s15.group4.sealcoffee.data.DummyData.employeeProfileData
import com.mobdeve.s15.group4.sealcoffee.data.DummyData.profileData

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val loginButton = findViewById<Button>(R.id.loginButton)

        // customer email = "mika.santos@gmail.com",
        // staff email = "carlo.staff@sealcoffee.com",
        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(applicationContext, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (email.contains("sealcoffee.com") || email == employeeProfileData.email) {

                Toast.makeText(
                    this,
                    "Welcome back, Staff: ${employeeProfileData.fullName}",
                    Toast.LENGTH_SHORT
                ).show()
                startActivity(Intent(this, EmployeeDashboardActivity::class.java))
                finish()
            } else {

                Toast.makeText(applicationContext, "Welcome back, ${profileData.fullName}", Toast.LENGTH_SHORT)
                    .show()
                startActivity(Intent(this, CustomerMenuActivity::class.java))
                finish()
            }



        }
        findViewById<TextView>(R.id.signUpLink).setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}
