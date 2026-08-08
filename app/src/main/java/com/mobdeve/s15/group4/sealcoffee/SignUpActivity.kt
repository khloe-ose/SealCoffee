package com.mobdeve.s15.group4.sealcoffee

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Locale

class SignUpActivity : AppCompatActivity() {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (auth.currentUser != null) {
            startActivity(Intent(this, CustomerMenuActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
            return
        }

        setContentView(R.layout.activity_sign_up)

        val firstName = findViewById<EditText>(R.id.firstNameInput)
        val lastName = findViewById<EditText>(R.id.lastNameInput)
        val birthDate = findViewById<EditText>(R.id.birthdateInput)
        val email = findViewById<EditText>(R.id.signUpEmailInput)
        val contact = findViewById<EditText>(R.id.contactNumberInput)
        val password = findViewById<EditText>(R.id.signUpPasswordInput)
        val createButton = findViewById<Button>(R.id.createAccountButton)

        birthDate.isFocusable = false
        birthDate.isClickable = true
        birthDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.YEAR, -18)
            val maxAllowedMillis = calendar.timeInMillis

            DatePickerDialog(
                this,
                { _, year, month, day ->
                    birthDate.setText(String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day))
                    birthDate.error = null
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.maxDate = maxAllowedMillis
            }.show()
        }

        createButton.setOnClickListener {
            listOf(firstName, lastName, birthDate, email, contact, password).forEach { it.error = null }

            val fNameStr = firstName.text.toString().trim()
            val lNameStr = lastName.text.toString().trim()
            val dobStr = birthDate.text.toString().trim()
            val emailStr = email.text.toString().trim()
            val contactStr = contact.text.toString().trim()
            val passStr = password.text.toString()
            val cleanContact = contactStr.replace(Regex("[^0-9]"), "")

            var hasError = false

            if (fNameStr.isEmpty()) {
                firstName.error = "First name is required."
                hasError = true
            }

            if (lNameStr.isEmpty()) {
                lastName.error = "Last name is required."
                hasError = true
            }

            if (dobStr.isEmpty()) {
                birthDate.error = "Birthdate is required."
                birthDate.requestFocus()
                hasError = true
            }

            if (emailStr.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(emailStr).matches()) {
                email.error = "Please enter a valid email address."
                hasError = true
            }

            if (contactStr.isEmpty() || cleanContact.length < 10 || cleanContact.length > 12) {
                contact.error = "Please enter a valid contact number."
                hasError = true
            }

            if (passStr.length < 8) {
                password.error = "Password must be at least 8 characters long."
                hasError = true
            } else if (!passStr.any { it.isDigit() } || !passStr.any { it.isLetter() }) {
                password.error = "Password must include both letters and numbers."
                hasError = true
            }

            if (hasError) return@setOnClickListener

            createButton.isEnabled = false

            lifecycleScope.launch {
                try {

                    val authResult = auth.createUserWithEmailAndPassword(emailStr, passStr).await()
                    val uid = authResult.user?.uid ?: throw Exception("Failed to retrieve user UID.")

                    val fullName = "$fNameStr $lNameStr"
                    val userDoc = mapOf(
                        "uid" to uid,
                        "email" to emailStr,
                        "fullName" to fullName,
                        "firstName" to fNameStr,
                        "lastName" to lNameStr,
                        "birthDate" to dobStr,
                        "contactNumber" to contactStr,
                        "role" to "customer",
                        "createdAt" to System.currentTimeMillis()
                    )

                    firestore.collection("users").document(uid).set(userDoc).await()

                    Toast.makeText(this@SignUpActivity, "Welcome Customer!", Toast.LENGTH_SHORT).show()

                    startActivity(Intent(this@SignUpActivity, CustomerMenuActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    finish()

                } catch (e: Exception) {
                    createButton.isEnabled = true
                    val errorMessage = e.localizedMessage ?: "Unknown error"

                    android.util.Log.e("SignUpActivity", "Registration/Firestore error: ", e)

                    if (errorMessage.contains("email address is already in use", ignoreCase = true) ||
                        e is FirebaseAuthUserCollisionException) {
                        email.error = "Unable to complete registration with this email. Please try logging in or use a different address."
                        email.requestFocus()
                    } else {
                        Snackbar.make(createButton, "Error: $errorMessage", Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }

        findViewById<TextView>(R.id.loginLink).setOnClickListener {
            finish()
        }
    }
}