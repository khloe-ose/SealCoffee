package com.mobdeve.s15.group4.sealcoffee

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.mobdeve.s15.group4.sealcoffee.data.DummyData

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        CustomerNavigation.bind(this, CustomerDestination.PROFILE)

        val profile = DummyData.profileData
        findViewById<TextView>(R.id.profileInitialsText).text = profile.avatarInitials
        findViewById<TextView>(R.id.profileNameText).text = profile.fullName
        findViewById<TextView>(R.id.profileBirthdayText).text = profile.birthday
        findViewById<TextView>(R.id.profileEmailText).text = profile.email
        findViewById<TextView>(R.id.profileContactText).text = profile.phone

        findViewById<Button>(R.id.logoutButton).setOnClickListener {
            startActivity(
                Intent(this, LoginActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            finish()
        }
    }
}
