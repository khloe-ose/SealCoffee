package com.mobdeve.s15.group4.sealcoffee

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mobdeve.s15.group4.sealcoffee.domain.UserRole
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!AuthNavigation.requireRole(this, UserRole.CUSTOMER)) return
        setContentView(R.layout.activity_profile)

        CustomerNavigation.bind(this, CustomerDestination.PROFILE)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                sealApp.repository.observeUser(sealApp.session.userId).collect { profile ->
                    if (profile == null) {
                        sealApp.session.clear()
                        AuthNavigation.routeAuthenticated(this@ProfileActivity)
                        return@collect
                    }
                    findViewById<TextView>(R.id.profileInitialsText).text = profile.fullName.initials()
                    findViewById<TextView>(R.id.profileNameText).text = profile.fullName
                    findViewById<TextView>(R.id.profileBirthdayText).text = profile.birthDate
                    findViewById<TextView>(R.id.profileEmailText).text = profile.email
                    findViewById<TextView>(R.id.profileContactText).text = profile.contactNumber
                }
            }
        }

        findViewById<Button>(R.id.logoutButton).setOnClickListener {
            AuthNavigation.logout(this)
        }
    }
}
