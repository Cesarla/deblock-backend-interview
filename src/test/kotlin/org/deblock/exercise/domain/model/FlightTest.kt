package org.deblock.exercise.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

class FlightTest {

    @Test
    fun `creates valid Flight`() {
        val flight = Flight(
            airline = "Iberia",
            supplier = "SupplierX",
            fare = BigDecimal.valueOf(150),
            departureAirportCode = IATACode("MAD"),
            destinationAirportCode = IATACode("BCN"),
            departureDate = LocalDate.of(2025, 5, 10),
            arrivalDate = LocalDate.of(2025, 5, 11)
        )

        assertEquals("Iberia", flight.airline)
        assertEquals("SupplierX", flight.supplier)
        assertEquals(BigDecimal.valueOf(150), flight.fare)
        assertEquals("MAD", flight.departureAirportCode.code)
        assertEquals("BCN", flight.destinationAirportCode.code)
        assertEquals(LocalDate.of(2025, 5, 10), flight.departureDate)
        assertEquals(LocalDate.of(2025, 5, 11), flight.arrivalDate)
    }

    @Test
    fun `throws exception when departure date is after arrival date`() {
        val ex = assertThrows(IllegalArgumentException::class.java) {
            Flight(
                airline = "Iberia",
                supplier = "SupplierX",
                fare = BigDecimal.valueOf(150),
                departureAirportCode = IATACode("MAD"),
                destinationAirportCode = IATACode("BCN"),
                departureDate = LocalDate.of(2025, 5, 12),
                arrivalDate = LocalDate.of(2025, 5, 11)
            )
        }
        assertEquals("Departure date cannot be after the arrival date", ex.message)
    }

    @Test
    fun `throws exception when fare is negative`() {
        val ex = assertThrows(IllegalArgumentException::class.java) {
            Flight(
                airline = "Iberia",
                supplier = "SupplierX",
                fare = BigDecimal.valueOf(-50),
                departureAirportCode = IATACode("MAD"),
                destinationAirportCode = IATACode("BCN"),
                departureDate = LocalDate.of(2025, 5, 10),
                arrivalDate = LocalDate.of(2025, 5, 11)
            )
        }
        assertEquals("Fare must be bigger or equal than zero", ex.message)
    }
}
