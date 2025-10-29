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
    fun `returns results from successful suppliers when one supplier fails`() {
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
        val successfulFlight = Flight(
            supplier = "OK",
            airline = "Vueling",
            fare = BigDecimal("150.00"),
            departureAirportCode = IATACode("BCN"),
            destinationAirportCode = IATACode("MAD"),
            departureDate = LocalDate.of(2025, 1, 20),
            arrivalDate = LocalDate.of(2025, 1, 20)
        )

        coEvery { failing.searchFlights(request) } throws IllegalStateException("boom")
        coEvery { failing.supplier() } returns "failing"
        coEvery { ok.searchFlights(request) } returns listOf(successfulFlight)
        coEvery { ok.supplier() } returns "ok"

        val service = FlightService(listOf(failing, ok))

        // when
        val flights = service.searchFlights(request)

        // then
        assertEquals(1, flights.size)
        assertEquals(successfulFlight, flights[0])
        coVerify(exactly = 1) { failing.searchFlights(request) }
        coVerify(exactly = 1) { ok.searchFlights(request) }
    }

    @Test
    fun `returns results from all successful suppliers when multiple suppliers fail`() {
        // given
        val failingA = mockk<FlightSupplierPort>()
        val failingB = mockk<FlightSupplierPort>()
        val okSupplier = mockk<FlightSupplierPort>()
        val request = FlightSearchRequest(
            origin = IATACode("BCN"),
            destination = IATACode("MAD"),
            departureDate = LocalDate.of(2025, 1, 20),
            returnDate = LocalDate.of(2025, 1, 28),
            numberOfPassengers = 1
        )

        val successfulFlights = listOf(
            Flight(
                supplier = "OK",
                airline = "Vueling",
                fare = BigDecimal("120.00"),
                departureAirportCode = IATACode("BCN"),
                destinationAirportCode = IATACode("MAD"),
                departureDate = LocalDate.of(2025, 1, 20),
                arrivalDate = LocalDate.of(2025, 1, 20)
            ),
            Flight(
                supplier = "OK",
                airline = "Iberia",
                fare = BigDecimal("180.00"),
                departureAirportCode = IATACode("BCN"),
                destinationAirportCode = IATACode("MAD"),
                departureDate = LocalDate.of(2025, 1, 20),
                arrivalDate = LocalDate.of(2025, 1, 20)
            )
        )

        coEvery { failingA.searchFlights(request) } throws IllegalStateException("timeout")
        coEvery { failingA.supplier() } returns "failing-a"
        coEvery { failingB.searchFlights(request) } throws RuntimeException("connection error")
        coEvery { failingB.supplier() } returns "failing-b"
        coEvery { okSupplier.searchFlights(request) } returns successfulFlights
        coEvery { okSupplier.supplier() } returns "ok"

        val service = FlightService(listOf(failingA, okSupplier, failingB))

        // when
        val flights = service.searchFlights(request)

        // then
        assertEquals(2, flights.size)
        assertEquals(successfulFlights[0], flights[0])
        assertEquals(successfulFlights[1], flights[1])
        coVerify(exactly = 1) { failingA.searchFlights(request) }
        coVerify(exactly = 1) { failingB.searchFlights(request) }
        coVerify(exactly = 1) { okSupplier.searchFlights(request) }
    }

    @Test
    fun `returns empty list when all suppliers fail`() {
        // given
        val failingA = mockk<FlightSupplierPort>()
        val failingB = mockk<FlightSupplierPort>()
        val request = FlightSearchRequest(
            origin = IATACode("BCN"),
            destination = IATACode("MAD"),
            departureDate = LocalDate.of(2025, 1, 20),
            returnDate = LocalDate.of(2025, 1, 28),
            numberOfPassengers = 1
        )

        coEvery { failingA.searchFlights(request) } throws IllegalStateException("boom")
        coEvery { failingA.supplier() } returns "failing-a"
        coEvery { failingB.searchFlights(request) } throws RuntimeException("crash")
        coEvery { failingB.supplier() } returns "failing-b"
        val service = FlightService(listOf(failingA, failingB))

        // when
        val flights = service.searchFlights(request)

        // then
        assertEquals(emptyList<Flight>(), flights)
        coVerify(exactly = 1) { failingA.searchFlights(request) }
        coVerify(exactly = 1) { failingB.searchFlights(request) }
    }
}
