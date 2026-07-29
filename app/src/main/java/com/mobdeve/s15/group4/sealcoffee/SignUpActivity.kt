package com.mobdeve.s15.group4.sealcoffee

import android.content.Intent
import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.mobdeve.s15.group4.sealcoffee.data.RegistrationResult
import com.mobdeve.s15.group4.sealcoffee.domain.RegistrationField
import com.mobdeve.s15.group4.sealcoffee.domain.RegistrationInput
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (sealApp.session.hasSession) {
            AuthNavigation.routeAuthenticated(this)
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
            val calendar = Calendar.getInstance().apply { add(Calendar.YEAR, -18) }
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
                datePicker.maxDate = System.currentTimeMillis()
            }.show()
        }

        createButton.setOnClickListener {
            listOf(firstName, lastName, birthDate, email, contact, password).forEach { it.error = null }
            val input = RegistrationInput(
                firstName = firstName.text.toString(),
                lastName = lastName.text.toString(),
                birthDateIso = birthDate.text.toString(),
                email = email.text.toString(),
                contactNumber = contact.text.toString(),
                password = password.text.toString()
            )
            createButton.isEnabled = false
            lifecycleScope.launch {
                when (val result = sealApp.repository.register(input)) {
                    is RegistrationResult.Success -> {
                        sealApp.session.save(result.user)
                        Snackbar.make(createButton, R.string.account_created, Snackbar.LENGTH_SHORT).show()
                        AuthNavigation.routeAuthenticated(this@SignUpActivity)
                    }
                    is RegistrationResult.Invalid -> {
                        result.errors.forEach { (field, message) ->
                            when (field) {
                                RegistrationField.FIRST_NAME -> firstName.error = message
                                RegistrationField.LAST_NAME -> lastName.error = message
                                RegistrationField.BIRTH_DATE -> birthDate.error = message
                                RegistrationField.EMAIL -> email.error = message
                                RegistrationField.CONTACT_NUMBER -> contact.error = message
                                RegistrationField.PASSWORD -> password.error = message
                            }
                        }
                        createButton.isEnabled = true
                    }
                    RegistrationResult.DuplicateEmail -> {
                        email.error = getString(R.string.email_already_registered)
                        email.requestFocus()
                        createButton.isEnabled = true
                    }
                    is RegistrationResult.Failure -> {
                        Snackbar.make(createButton, result.message, Snackbar.LENGTH_LONG).show()
                        createButton.isEnabled = true
                    }
                }
            }
        }

        findViewById<TextView>(R.id.loginLink).setOnClickListener {
            finish()
        }
    }
}