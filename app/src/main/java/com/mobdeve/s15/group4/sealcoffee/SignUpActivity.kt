package com.mobdeve.s15.group4.sealcoffee

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        findViewById<Button>(R.id.createAccountButton).setOnClickListener {
            startActivity(Intent(this, CustomerMenuActivity::class.java))
        }

        findViewById<TextView>(R.id.loginLink).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
