package org.deblock.exercise.domain.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class IATACodeTest {

    @Test
    fun `creates IATACode when code has exactly 3 characters`() {
        val code = IATACode("BCN")
        assertEquals("BCN", code.code)
    }

    @Test
    fun `throws IllegalArgumentException when code is shorter than 3 characters`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            IATACode("NY")
        }
        assertEquals("IATA CODE must match ^[A-Z0-9]{3}$", exception.message)
    }

    @Test
    fun `throws IllegalArgumentException when code is longer than 3 characters`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            IATACode("Barcelona")
        }
        assertEquals("IATA CODE must match ^[A-Z0-9]{3}$", exception.message)
    }

    @Test
    fun `throws IllegalArgumentException when code is empty`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            IATACode("")
        }
        assertEquals("IATA CODE must match ^[A-Z0-9]{3}$", exception.message)
    }

    @Test
    fun `throws on lowercase`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            IATACode("bcn")
        }
        assertEquals("IATA CODE must match ^[A-Z0-9]{3}$", exception.message)
    }
}