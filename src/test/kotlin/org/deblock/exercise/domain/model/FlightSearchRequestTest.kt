package org.deblock.exercise.domain.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate

class FlightSearchRequestTest {
    @Test
    fun `creates valid FlightSearchRequest`() {
        val request = FlightSearchRequest(
            origin = IATACode("MAD"),
            destination = IATACode("BCN"),
            departureDate = LocalDate.of(2025, 5, 10),
            returnDate = LocalDate.of(2025, 5, 20),
            numberOfPassengers = 2
        )

        assertEquals("MAD", request.origin.code)
        assertEquals("BCN", request.destination.code)
        assertEquals(LocalDate.of(2025, 5, 10), request.departureDate)
        assertEquals(LocalDate.of(2025, 5, 20), request.returnDate)
        assertEquals(2, request.numberOfPassengers)
    }

    @Test
    fun `throws exception when passengers bellow 1`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            FlightSearchRequest(
                origin = IATACode("MAD"),
                destination = IATACode("BCN"),
                departureDate = LocalDate.of(2025, 5, 10),
                returnDate = LocalDate.of(2025, 5, 20),
                numberOfPassengers = 0
            )
        }

        assertEquals("Number of passengers must be between 1 and 4", exception.message)
    }

    @Test
    fun `throws exception when passengers exceed 4`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            FlightSearchRequest(
                origin = IATACode("MAD"),
                destination = IATACode("BCN"),
                departureDate = LocalDate.of(2025, 5, 10),
                returnDate = LocalDate.of(2025, 5, 20),
                numberOfPassengers = 5
            )
        }

        assertEquals("Number of passengers must be between 1 and 4", exception.message)
    }

    @Test
    fun `throws exception when return date is before departure date`() {
        val ex = assertThrows(IllegalArgumentException::class.java) {
            FlightSearchRequest(
                origin = IATACode("MAD"),
                destination = IATACode("BCN"),
                departureDate = LocalDate.of(2025, 5, 20),
                returnDate = LocalDate.of(2025, 5, 10),
                numberOfPassengers = 2
            )
        }
        assertEquals("Departure date must be before the return date", ex.message)
    }
}