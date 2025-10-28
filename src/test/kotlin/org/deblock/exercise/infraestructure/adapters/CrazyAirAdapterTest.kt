package org.deblock.exercise.infraestructure.adapters

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.deblock.exercise.domain.model.Flight
import org.deblock.exercise.domain.model.FlightSearchRequest
import org.deblock.exercise.domain.model.IATACode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.web.client.RestTemplate
import java.math.BigDecimal
import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime

class CrazyAirAdapterTest {

    private lateinit var restTemplate: RestTemplate
    private lateinit var adapter: CrazyAirAdapter

    @BeforeEach
    fun setUp() {
        restTemplate = mockk()
        adapter = CrazyAirAdapter(restTemplate)
    }

    @Test
    fun `posts CrazyAirRequest with all fields to CrazyAir endpoint`() {
        runBlocking {
            // given
            val request = FlightSearchRequest(
                origin = IATACode("MAD"),
                destination = IATACode("BCN"),
                departureDate = LocalDate.of(2025, 1, 20),
                returnDate = LocalDate.of(2025, 1, 28),
                numberOfPassengers = 2
            )
            val urlSlot = slot<URI>()
            val bodySlot = slot<CrazyAirAdapter.CrazyAirRequest>()
            val expected = CrazyAirAdapter.CrazyAirRequest(
                origin = IATACode("MAD"),
                destination = IATACode("BCN"),
                departureDate = LocalDate.of(2025, 1, 20),
                returnDate = LocalDate.of(2025, 1, 28),
                passengerCount = 2
            )

            every {
                restTemplate.postForObject(
                    capture(urlSlot),
                    capture(bodySlot),
                    Array<CrazyAirAdapter.CrazyAirResponse>::class.java
                )
            } returns emptyArray()

            // when
            adapter.searchFlights(request)

            // then
            verify(exactly = 1) {
                restTemplate.postForObject(
                    any<URI>(),
                    any(),
                    Array<CrazyAirAdapter.CrazyAirResponse>::class.java
                )
            }
            val url = urlSlot.captured
            val body = bodySlot.captured
            assertEquals(URI.create("https://api.crazyair.com/flights"), url)
            assertEquals(expected, body)
        }
    }

    @Test
    fun `maps CrazyAirResponse array to Flight list`() {
        runBlocking {
            // given
            val request = FlightSearchRequest(
                origin = IATACode("MAD"),
                destination = IATACode("BCN"),
                departureDate = LocalDate.of(2025, 1, 20),
                returnDate = LocalDate.of(2025, 1, 28),
                numberOfPassengers = 1
            )
            val response = arrayOf(
                CrazyAirAdapter.CrazyAirResponse(
                    airline = "Crazy Air",
                    price = BigDecimal("123.45"),
                    departureAirportCode = IATACode("MAD"),
                    destinationAirportCode = IATACode("BCN"),
                    departureDate = LocalDateTime.of(2025, 1, 20, 10, 15, 0),
                    arrivalDate = LocalDateTime.of(2025, 1, 20, 18, 30, 0)
                )
            )
            val expected = listOf(
                Flight(
                    supplier = "CrazyAir",
                    airline = "Crazy Air",
                    fare = BigDecimal("123.45"),
                    departureAirportCode = IATACode("MAD"),
                    destinationAirportCode = IATACode("BCN"),
                    departureDate = LocalDate.of(2025, 1, 20),
                    arrivalDate = LocalDate.of(2025, 1, 20)
                )
            )

            every {
                restTemplate.postForObject(
                    any<URI>(),
                    any(),
                    Array<CrazyAirAdapter.CrazyAirResponse>::class.java
                )
            } returns response

            // when
            val flights = adapter.searchFlights(request)

            // then
            assertIterableEquals(expected, flights)
        }
    }

    @Test
    fun `returns empty list when API returns null body`() {
        runBlocking {
            // given
            val request = FlightSearchRequest(
                origin = IATACode("MAD"),
                destination = IATACode("BCN"),
                departureDate = LocalDate.of(2025, 1, 20),
                returnDate = LocalDate.of(2025, 1, 28),
                numberOfPassengers = 1
            )
            every {
                restTemplate.postForObject(
                    any<String>(),
                    any(),
                    Array<CrazyAirAdapter.CrazyAirResponse>::class.java
                )
            } returns null

            // when
            val flights = adapter.searchFlights(request)

            // then
            assertTrue(flights.isEmpty())
        }
    }

    @Test
    fun `exposes supplier name CrazyAir`() {
        // given / when
        val supplier = adapter.supplier()

        // then
        assertEquals("CrazyAir", supplier)
    }
}