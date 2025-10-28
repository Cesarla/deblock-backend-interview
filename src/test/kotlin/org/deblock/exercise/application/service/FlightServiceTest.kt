package org.deblock.exercise.application

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.deblock.exercise.application.service.FlightService
import org.deblock.exercise.domain.model.Flight
import org.deblock.exercise.domain.model.FlightSearchRequest
import org.deblock.exercise.domain.model.IATACode
import org.deblock.exercise.domain.port.outbound.FlightSupplierPort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

class FlightServiceTest {

    @Test
    fun `aggregates results from all suppliers and sorts by fare ascending`() {
        // given
        val supplierA = mockk<FlightSupplierPort>()
        val supplierB = mockk<FlightSupplierPort>()
        val request = FlightSearchRequest(
            origin = IATACode("BCB"),
            destination = IATACode("MAD"),
            departureDate = LocalDate.of(2025, 1, 20),
            returnDate = LocalDate.of(2025, 1, 28),
            numberOfPassengers = 1
        )
        coEvery { supplierA.searchFlights(request) } returns listOf(
            Flight(
                supplier = "A",
                airline = "Iberia",
                fare = BigDecimal("200.00"),
                departureAirportCode = IATACode("BCN"),
                destinationAirportCode = IATACode("MAD"),
                departureDate = LocalDate.of(2025, 1, 20),
                arrivalDate = LocalDate.of(2025, 1, 20)
            )
        )
        coEvery { supplierB.searchFlights(request) } returns listOf(
            Flight(
                supplier = "B",
                airline = "Vueling",
                fare = BigDecimal("150.00"),
                departureAirportCode = IATACode("BCN"),
                destinationAirportCode = IATACode("MAD"),
                departureDate = LocalDate.of(2025, 1, 20),
                arrivalDate = LocalDate.of(2025, 1, 20)
            ),
            Flight(
                supplier = "B",
                airline = "Volotea",
                fare = BigDecimal("250.00"),
                departureAirportCode = IATACode("BCN"),
                destinationAirportCode = IATACode("MAD"),
                departureDate = LocalDate.of(2025, 1, 20),
                arrivalDate = LocalDate.of(2025, 1, 20)
            )
        )
        val service = FlightService(listOf(supplierA, supplierB))

        // when
        val flights = service.searchFlights(request)

        // then
        val expected = listOf(
            Flight(
                supplier = "B",
                airline = "Vueling",
                fare = BigDecimal("150.00"),
                departureAirportCode = IATACode("BCN"),
                destinationAirportCode = IATACode("MAD"),
                departureDate = LocalDate.of(2025, 1, 20),
                arrivalDate = LocalDate.of(2025, 1, 20)
            ),
            Flight(
                supplier = "A",
                airline = "Iberia",
                fare = BigDecimal("200.00"),
                departureAirportCode = IATACode("BCN"),
                destinationAirportCode = IATACode("MAD"),
                departureDate = LocalDate.of(2025, 1, 20),
                arrivalDate = LocalDate.of(2025, 1, 20)
            ),
            Flight(
                supplier = "B",
                airline = "Volotea",
                fare = BigDecimal("250.00"),
                departureAirportCode = IATACode("BCN"),
                destinationAirportCode = IATACode("MAD"),
                departureDate = LocalDate.of(2025, 1, 20),
                arrivalDate = LocalDate.of(2025, 1, 20)
            )
        )
        assertEquals(expected, flights)
        coVerify(exactly = 1) { supplierA.searchFlights(request) }
        coVerify(exactly = 1) { supplierB.searchFlights(request) }
    }

    @Test
    fun `returns empty when no suppliers`() {
        // given
        val service = FlightService(emptyList())
        val request = FlightSearchRequest(
            origin = IATACode("BCN"),
            destination = IATACode("MAD"),
            departureDate = LocalDate.of(2025, 1, 20),
            returnDate = LocalDate.of(2025, 1, 28),
            numberOfPassengers = 1
        )

        // when
        val flights = service.searchFlights(request)

        // then
        assertEquals(emptyList<Flight>(), flights)
    }

    @Test
    fun `propagates supplier exception from coroutineScope`() {
        // given
        val failing = mockk<FlightSupplierPort>()
        val ok = mockk<FlightSupplierPort>()
        val request = FlightSearchRequest(
            origin = IATACode("BCN"),
            destination = IATACode("MAD"),
            departureDate = LocalDate.of(2025, 1, 20),
            returnDate = LocalDate.of(2025, 1, 28),
            numberOfPassengers = 1
        )
        coEvery { failing.searchFlights(request) } throws IllegalStateException("boom")
        coEvery { ok.searchFlights(request) } returns emptyList()
        val service = FlightService(listOf(failing, ok))

        // when
        val thrown = assertThrows(IllegalStateException::class.java) {
            service.searchFlights(request)
        }

        // then
        assertEquals("boom", thrown.message)
    }
}
