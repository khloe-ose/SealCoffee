package com.mobdeve.s15.group4.sealcoffee

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class EmployeeLoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_employee_login)

        findViewById<Button>(R.id.employeeLoginButton).setOnClickListener {
            startActivity(Intent(this, EmployeeDashboardActivity::class.java))
        }

        findViewById<TextView>(R.id.customerLoginLink).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
