package com.mobdeve.s15.group4.sealcoffee

import com.mobdeve.s15.group4.sealcoffee.domain.RegistrationField
import com.mobdeve.s15.group4.sealcoffee.domain.RegistrationInput
import com.mobdeve.s15.group4.sealcoffee.domain.RegistrationValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale

class RegistrationValidatorTest {
    @Test
    fun validRegistration_isAcceptedAndEmailIsNormalised() {
        val input = validInput()

        assertTrue(RegistrationValidator.validate(input).isEmpty())
        assertEquals("mika.santos@gmail.com", RegistrationValidator.normaliseEmail(input.email))
    }

    @Test
    fun invalidFields_returnFieldSpecificErrors() {
        val input = RegistrationInput("", "X", "2999-01-01", "bad", "12", "weak")
        val now = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse("2026-07-25")!!
        val errors = RegistrationValidator.validate(input, now)

        assertTrue(RegistrationField.FIRST_NAME in errors)
        assertTrue(RegistrationField.LAST_NAME in errors)
        assertTrue(RegistrationField.BIRTH_DATE in errors)
        assertTrue(RegistrationField.EMAIL in errors)
        assertTrue(RegistrationField.CONTACT_NUMBER in errors)
        assertTrue(RegistrationField.PASSWORD in errors)
    }

    private fun validInput() = RegistrationInput(
        firstName = "Mika",
        lastName = "Santos",
        birthDateIso = "2002-03-14",
        email = "  Mika.Santos@GMAIL.com ",
        contactNumber = "+639175550148",
        password = "Coffee123!"
    )
}
