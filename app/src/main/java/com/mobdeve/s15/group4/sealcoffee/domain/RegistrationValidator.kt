package com.mobdeve.s15.group4.sealcoffee.domain

import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class RegistrationInput(
    val firstName: String,
    val lastName: String,
    val birthDateIso: String,
    val email: String,
    val contactNumber: String,
    val password: String
)

enum class RegistrationField {
    FIRST_NAME, LAST_NAME, BIRTH_DATE, EMAIL, CONTACT_NUMBER, PASSWORD
}

object RegistrationValidator {
    private val emailPattern = Regex("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", RegexOption.IGNORE_CASE)
    private val contactPattern = Regex("^\\+?[0-9]{7,15}$")

    fun normaliseEmail(email: String): String = email.trim().lowercase(Locale.ROOT)

    fun validate(input: RegistrationInput, now: Date = Date()): Map<RegistrationField, String> {
        val errors = linkedMapOf<RegistrationField, String>()
        if (input.firstName.trim().length < 2) errors[RegistrationField.FIRST_NAME] = "Enter a valid first name"
        if (input.lastName.trim().length < 2) errors[RegistrationField.LAST_NAME] = "Enter a valid last name"

        val birthDate = parseIsoDate(input.birthDateIso)
        if (birthDate == null) {
            errors[RegistrationField.BIRTH_DATE] = "Choose a valid birthday"
        } else if (birthDate.after(now)) {
            errors[RegistrationField.BIRTH_DATE] = "Birthday cannot be in the future"
        }

        if (!emailPattern.matches(normaliseEmail(input.email))) {
            errors[RegistrationField.EMAIL] = "Enter a valid email address"
        }
        val normalisedContact = input.contactNumber.filterNot(Char::isWhitespace).replace("-", "")
        if (!contactPattern.matches(normalisedContact)) {
            errors[RegistrationField.CONTACT_NUMBER] = "Use 7–15 digits, optionally starting with +"
        }
        val password = input.password
        if (
            password.length < 8 ||
            password.none(Char::isUpperCase) ||
            password.none(Char::isLowerCase) ||
            password.none(Char::isDigit) ||
            password.none { !it.isLetterOrDigit() }
        ) {
            errors[RegistrationField.PASSWORD] = "Use 8+ characters with upper, lower, number, and symbol"
        }
        return errors
    }

    fun parseIsoDate(value: String): Date? {
        val trimmed = value.trim()
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
        val position = ParsePosition(0)
        val date = formatter.parse(trimmed, position)
        return if (date != null && position.index == trimmed.length) date else null
    }
}
