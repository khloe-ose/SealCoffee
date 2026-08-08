package com.mobdeve.s15.group4.sealcoffee

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object FirestoreUserSeedUtility {

    data class SeedUser(
        val email: String,
        val pass: String,
        val firstName: String,
        val lastName: String,
        val birthDate: String,
        val contactNumber: String,
        val role: String // "customer" or "employee"
    )

    fun seedUsersIfNeeded() {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val usersRef = db.collection("users")

        val seedUsers = listOf(
            SeedUser("mika.santos@gmail.com", "Coffee123!", "Mika", "Santos", "2002-03-14", "+63 917 555 0148", "customer"),
            SeedUser("carlo.staff@sealcoffee.com", "Staff123!", "Carlo", "Dela Cruz", "1999-08-09", "+63 917 555 0192", "employee"),
            SeedUser("cheska.martinez@gmail.com", "CoffeeNo1!", "Cheska", "Martinez", "2001-11-20", "+63 918 555 0234", "customer"),
            SeedUser("steven.universe@gmail.com", "Cookie123!", "Steven", "Universe", "2000-05-15", "+63 919 555 0389", "customer")
        )

        CoroutineScope(Dispatchers.IO).launch {
            for (user in seedUsers) {
                try {
                    val querySnapshot = usersRef.whereEqualTo("email", user.email).get().await()

                    var uid: String? = null

                    if (querySnapshot.isEmpty) {
                        uid = try {
                            val authResult = auth.createUserWithEmailAndPassword(user.email, user.pass).await()
                            authResult.user?.uid
                        } catch (e: Exception) {
                            Log.w("UserSeed", "Auth user likely already exists for ${user.email}: ${e.localizedMessage}")
                            null
                        }
                    } else {
                        uid = querySnapshot.documents.first().getString("uid")
                            ?: querySnapshot.documents.first().id
                    }

                    if (uid != null) {
                        val fullName = "${user.firstName} ${user.lastName}"
                        val userDoc = mutableMapOf<String, Any>(
                            "uid" to uid,
                            "email" to user.email,
                            "fullName" to fullName,
                            "firstName" to user.firstName,
                            "lastName" to user.lastName,
                            "birthDate" to user.birthDate,
                            "contactNumber" to user.contactNumber,
                            "role" to user.role,
                            "createdAt" to FieldValue.serverTimestamp()
                        )

                        usersRef.document(uid).set(userDoc, com.google.firebase.firestore.SetOptions.merge()).await()
                        Log.d("UserSeed", "Successfully synced user profile: ${user.email}")
                    }
                } catch (e: Exception) {
                    Log.e("UserSeed", "Failed processing seed user ${user.email}", e)
                }
            }
        }
    }
}