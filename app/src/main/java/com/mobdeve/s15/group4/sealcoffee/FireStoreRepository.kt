package com.mobdeve.s15.group4.sealcoffee

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class AppUser(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "CUSTOMER"
)

class FireStoreRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    suspend fun getUser(uid: String): AppUser? {
        return try {
            val doc = db.collection("users").document(uid).get().await()
            if (doc.exists()) {
                doc.toObject(AppUser::class.java)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    suspend fun authenticate(email: String, pass: String): AppUser? {
        return try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            val uid = result.user?.uid ?: return null
            getUser(uid)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun register(name: String, email: String, pass: String): Result<AppUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val uid = result.user?.uid ?: throw Exception("Authentication user ID is null")

            val newUser = AppUser(
                uid = uid,
                name = name,
                email = email,
                role = "CUSTOMER"
            )

            db.collection("users").document(uid).set(newUser).await()
            Result.success(newUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}