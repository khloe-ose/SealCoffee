package com.mobdeve.s15.group4.sealcoffee

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private val profileInitialsText by lazy { findViewById<TextView>(R.id.profileInitialsText) }
    private val profileNameText by lazy { findViewById<TextView>(R.id.profileNameText) }
    private val profileBirthdayText by lazy { findViewById<TextView>(R.id.profileBirthdayText) }
    private val profileEmailText by lazy { findViewById<TextView>(R.id.profileEmailText) }
    private val profileContactText by lazy { findViewById<TextView>(R.id.profileContactText) }
    private val logoutButton by lazy { findViewById<Button>(R.id.logoutButton) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        CustomerNavigation.bind(this, CustomerDestination.PROFILE)
    }

    override fun onResume() {
        super.onResume()

        val currentUser = auth.currentUser
        if (currentUser == null || currentUser.email == null) {
            redirectToLogin()
            return
        }

        // Query by email so seed accounts with custom IDs (like "seed_customer_mika") resolve correctly
        firestore.collection("users")
            .whereEqualTo("email", currentUser.email)
            .limit(1)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!isFinishing && !isDestroyed) {
                    if (!querySnapshot.isEmpty) {
                        val document = querySnapshot.documents[0]

                        val fullName = document.getString("fullName")
                            ?: "${document.getString("firstName") ?: ""} ${document.getString("LastName") ?: document.getString("lastName") ?: ""}".trim()
                                .ifEmpty { "Customer" }

                        val email = document.getString("email") ?: currentUser.email ?: ""

                        val birthday = document.getString("birthDate")
                            ?: document.getString("birthday")
                            ?: document.getString("birthdate")
                            ?: "Not provided"

                        val contact = document.getString("contactNumber")
                            ?: document.getString("contact")
                            ?: document.getString("phoneNumber")
                            ?: "Not provided"

                        profileInitialsText.text = fullName.initials()
                        profileNameText.text = fullName
                        profileBirthdayText.text = birthday
                        profileEmailText.text = email
                        profileContactText.text = contact
                    } else {
                        applyFallback(currentUser.email!!)
                    }
                }
            }
            .addOnFailureListener {
                if (!isFinishing && !isDestroyed) {
                    applyFallback(currentUser.email!!)
                }
            }

        logoutButton.setOnClickListener {
            auth.signOut()
            redirectToLogin()
        }
    }

    private fun applyFallback(email: String) {
        profileInitialsText.text = "C"
        profileNameText.text = "Customer"
        profileEmailText.text = email
        profileBirthdayText.text = "Not provided"
        profileContactText.text = "Not provided"
    }

    private fun redirectToLogin() {
        val intent = Intent(this, OnboardingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}