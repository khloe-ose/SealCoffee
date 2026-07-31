package com.mobdeve.s15.group4.sealcoffee

import com.mobdeve.s15.group4.sealcoffee.domain.PasswordHasher
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordHasherTest {
    @Test
    fun saltedHash_verifiesOnlyTheCorrectPassword() {
        val digest = PasswordHasher.create("Coffee123!".toCharArray())

        assertNotEquals("Coffee123!", digest.hash)
        assertNotEquals("Coffee123!", digest.salt)
        assertTrue(PasswordHasher.verify("Coffee123!".toCharArray(), digest.hash, digest.salt))
        assertFalse(PasswordHasher.verify("Wrong123!".toCharArray(), digest.hash, digest.salt))
    }
}
