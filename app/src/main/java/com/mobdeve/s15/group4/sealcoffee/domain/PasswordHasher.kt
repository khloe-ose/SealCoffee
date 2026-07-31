package com.mobdeve.s15.group4.sealcoffee.domain

import java.nio.ByteBuffer
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

data class PasswordDigest(val hash: String, val salt: String)

/**
 * PBKDF2-HMAC-SHA256 implemented with Android's widely available HmacSHA256 provider.
 */
object PasswordHasher {
    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH_BYTES = 32
    private const val SALT_LENGTH_BYTES = 16
    private const val HMAC_ALGORITHM = "HmacSHA256"
    private val secureRandom = SecureRandom()

    fun create(password: CharArray): PasswordDigest {
        require(password.isNotEmpty()) { "Password cannot be empty" }
        val salt = ByteArray(SALT_LENGTH_BYTES).also(secureRandom::nextBytes)
        return PasswordDigest(derive(password, salt).toHex(), salt.toHex())
    }

    fun verify(password: CharArray, expectedHash: String, salt: String): Boolean {
        val expectedBytes = expectedHash.hexToBytesOrNull() ?: return false
        val saltBytes = salt.hexToBytesOrNull() ?: return false
        return MessageDigest.isEqual(derive(password, saltBytes), expectedBytes)
    }

    private fun derive(password: CharArray, salt: ByteArray): ByteArray {
        val passwordBytes = password.concatToString().toByteArray(Charsets.UTF_8)
        try {
            val mac = Mac.getInstance(HMAC_ALGORITHM)
            mac.init(SecretKeySpec(passwordBytes, HMAC_ALGORITHM))
            val hashLength = mac.macLength
            val blockCount = (KEY_LENGTH_BYTES + hashLength - 1) / hashLength
            val result = ByteArray(blockCount * hashLength)
            var resultOffset = 0
            for (blockIndex in 1..blockCount) {
                mac.update(salt)
                var u = mac.doFinal(ByteBuffer.allocate(4).putInt(blockIndex).array())
                val block = u.copyOf()
                repeat(ITERATIONS - 1) {
                    u = mac.doFinal(u)
                    for (index in block.indices) {
                        block[index] = (block[index].toInt() xor u[index].toInt()).toByte()
                    }
                }
                block.copyInto(result, resultOffset)
                resultOffset += block.size
            }
            return result.copyOf(KEY_LENGTH_BYTES)
        } finally {
            passwordBytes.fill(0)
        }
    }

    private fun ByteArray.toHex(): String = joinToString(separator = "") { "%02x".format(it) }

    private fun String.hexToBytesOrNull(): ByteArray? {
        if (length % 2 != 0) return null
        return runCatching {
            ByteArray(length / 2) { index ->
                substring(index * 2, index * 2 + 2).toInt(16).toByte()
            }
        }.getOrNull()
    }
}
