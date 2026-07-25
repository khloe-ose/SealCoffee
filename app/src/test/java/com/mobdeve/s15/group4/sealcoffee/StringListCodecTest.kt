package com.mobdeve.s15.group4.sealcoffee

import com.mobdeve.s15.group4.sealcoffee.data.StringListCodec
import org.junit.Assert.assertEquals
import org.junit.Test

class StringListCodecTest {
    @Test
    fun listRoundTrip_removesBlanksAndDuplicates() {
        val encoded = StringListCodec.encode(listOf(" Oat milk ", "", "Caramel drizzle", "Oat milk"))
        assertEquals(listOf("Oat milk", "Caramel drizzle"), StringListCodec.decode(encoded))
    }
}
