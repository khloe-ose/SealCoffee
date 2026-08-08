package com.mobdeve.s15.group4.sealcoffee

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object AuthNavigation {

    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore get() = FirebaseFirestore.getInstance()

    fun requireAuthenticated(activity: Activity, onChecked: (Boolean) -> Unit = {}) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            redirectToLogin(activity)
            onChecked(false)
        } else {
            onChecked(true)
        }
    }

    fun requireRole(activity: Activity, expectedRole: String, onChecked: (Boolean) -> Unit = {}) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            redirectToLogin(activity)
            onChecked(false)
            return
        }

        db.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {

                    val email = currentUser.email
                    val fallbackRole = if (email != null && (email.contains("staff", ignoreCase = true) || email.endsWith("@sealcoffee.com"))) {
                        "employee"
                    } else {
                        "customer"
                    }

                    if (fallbackRole.equals(expectedRole, ignoreCase = true)) {
                        onChecked(true)
                    } else {
                        Toast.makeText(activity, R.string.session_required, Toast.LENGTH_SHORT).show()
                        redirectToLogin(activity)
                        onChecked(false)
                    }
                    return@addOnSuccessListener
                }

                val userRole = (document.getString("role") ?: document.getString("Role") ?: "").lowercase()
                val expected = expectedRole.lowercase()

                if (userRole == expected || (expected == "employee" && userRole == "staff")) {
                    onChecked(true)
                } else {
                    Toast.makeText(activity, R.string.session_required, Toast.LENGTH_SHORT).show()
                    redirectToLogin(activity)
                    onChecked(false)
                }
            }
            .addOnFailureListener {

                val email = currentUser.email
                val isStaffEmail = email != null && (email.contains("staff", ignoreCase = true) || email.endsWith("@sealcoffee.com"))
                if ((expectedRole.lowercase() == "employee" && isStaffEmail) || (expectedRole.lowercase() == "customer" && !isStaffEmail)) {
                    onChecked(true)
                } else {
                    Toast.makeText(activity, "Failed to verify user role", Toast.LENGTH_SHORT).show()
                    redirectToLogin(activity)
                    onChecked(false)
                }
            }
    }

    fun routeAuthenticated(activity: Activity) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            redirectToLogin(activity)
            return
        }

        db.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { document ->
                val roleString = if (document.exists()) {
                    (document.getString("role") ?: document.getString("Role") ?: "customer").lowercase()
                } else {

                    val email = currentUser.email
                    if (email != null && (email.contains("staff", ignoreCase = true) || email.endsWith("@sealcoffee.com"))) {
                        "employee"
                    } else {
                        "customer"
                    }
                }

                val destination = when (roleString) {
                    "customer" -> CustomerMenuActivity::class.java
                    "employee", "staff" -> EmployeeDashboardActivity::class.java
                    else -> CustomerMenuActivity::class.java
                }

                if (activity::class.java == destination) return@addOnSuccessListener

                activity.startActivity(
                    Intent(activity, destination)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
                activity.finish()
            }
            .addOnFailureListener {

                val email = currentUser.email
                val destination = if (email != null && (email.contains("staff", ignoreCase = true) || email.endsWith("@sealcoffee.com"))) {
                    EmployeeDashboardActivity::class.java
                } else {
                    CustomerMenuActivity::class.java
                }

                if (activity::class.java == destination) return@addOnFailureListener

                activity.startActivity(
                    Intent(activity, destination)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
                activity.finish()
            }
    }
    fun confirmLogout(activity: Activity) {
        AlertDialog.Builder(activity)
            .setTitle("Confirm Logout")
            .setMessage("Are you sure you want to log out of your session?")
            .setPositiveButton("Log Out") { _, _ ->
                logout(activity)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun logout(activity: Activity) {
        auth.signOut()
        redirectToLogin(activity)
    }

    private fun redirectToLogin(activity: Activity) {
        if (activity is LoginActivity) return

        activity.startActivity(
            Intent(activity, LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        activity.finish()
    }
}