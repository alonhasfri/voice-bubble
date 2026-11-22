package com.voicebubble

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TransferTest {
    @Test fun codeRoundTrip() {
        val c = Transfer.newCode(byteArrayOf(192.toByte(), 168.toByte(), 1, 23))
        assertEquals(19, c.text.length)   // 16 letters + 3 dashes
        val back = Transfer.parse(c.text.lowercase().replace("-", " "))!!
        assertEquals("192.168.1.23", back.host)
        assertArrayEquals(c.secret, back.secret)
    }

    @Test fun badCodes() {
        assertNull(Transfer.parse("hello"))
        assertNull(Transfer.parse("K7QD-2XMB-9FHT"))
    }

    @Test fun encryptRoundTrip() {
        val secret = byteArrayOf(1, 2, 3, 4, 5, 6)
        val data = Transfer.gzip("{\"v\":1}".toByteArray())
        assertEquals("{\"v\":1}", String(Transfer.gunzip(Transfer.decrypt(Transfer.encrypt(data, secret), secret))))
    }
}
