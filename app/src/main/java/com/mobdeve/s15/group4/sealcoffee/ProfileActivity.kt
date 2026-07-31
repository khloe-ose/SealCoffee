package com.mobdeve.s15.group4.sealcoffee

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mobdeve.s15.group4.sealcoffee.domain.UserRole
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!AuthNavigation.requireRole(this, UserRole.CUSTOMER)) return
        setContentView(R.layout.activity_profile)

        CustomerNavigation.bind(this, CustomerDestination.PROFILE)

        lifecycleScope.launch {
            try {
                val firebaseUser = FirebaseAuth.getInstance().currentUser
                if (firebaseUser != null) {
                    val docSnapshot = FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(firebaseUser.uid)
                        .get()
                        .await()

                    if (docSnapshot.exists()) {
                        val fullName = docSnapshot.getString("full_name") ?: "User"
                        val birthDate = docSnapshot.getString("birth_date") ?: ""
                        val email = docSnapshot.getString("email") ?: ""
                        val contactNumber = docSnapshot.getString("contact_number") ?: ""

                        findViewById<TextView>(R.id.profileInitialsText).text = fullName.initials()
                        findViewById<TextView>(R.id.profileNameText).text = fullName
                        findViewById<TextView>(R.id.profileBirthdayText).text = birthDate
                        findViewById<TextView>(R.id.profileEmailText).text = email
                        findViewById<TextView>(R.id.profileContactText).text = contactNumber
                    }
                }
            } catch (e: Exception) {

            }
        }

        findViewById<Button>(R.id.logoutButton).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            AuthNavigation.logout(this)
        }
    }
}
