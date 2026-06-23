package com.mobdeve.s15.group4.sealcoffee

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        findViewById<Button>(R.id.loginButton).setOnClickListener {
            startActivity(Intent(this, CustomerMenuActivity::class.java))
        }

        findViewById<TextView>(R.id.signUpLink).setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        findViewById<TextView>(R.id.employeeLoginLink).setOnClickListener {
            startActivity(Intent(this, EmployeeLoginActivity::class.java))
        }
    }
}
