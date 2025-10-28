package org.deblock.exercise.infraestructure.adapters

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.deblock.exercise.domain.model.Flight
import org.deblock.exercise.domain.model.FlightSearchRequest
import org.deblock.exercise.domain.model.IATACode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.client.RestTemplate
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

class ToughJetAdapterTest {

    private lateinit var restTemplate: RestTemplate
    private lateinit var adapter: ToughJetAdapter

    @BeforeEach
    fun setUp() {
        restTemplate = mockk()
        adapter = ToughJetAdapter(restTemplate)
    }

    @Test
    fun `posts ToughJetRequest with all fields to ToughJet endpoint`() {
        runBlocking {
            // given
            val req = FlightSearchRequest(
                origin = IATACode("BCN"),
                destination = IATACode("MAD"),
                departureDate = LocalDate.of(2025, 1, 20),
                returnDate = LocalDate.of(2025, 1, 28),
                numberOfPassengers = 2
            )
            val urlSlot = slot<String>()
            val bodySlot = slot<ToughJetAdapter.ToughJetRequest>()
            val expected = ToughJetAdapter.ToughJetRequest(
                from = IATACode("BCN"),
                to = IATACode("MAD"),
                inboundDate = LocalDate.of(2025, 1, 20),
                outboundDate = LocalDate.of(2025, 1, 28),
                numberOfAdults = 2
            )

            every {
                restTemplate.postForObject(
                    capture(urlSlot),
                    capture(bodySlot),
                    Array<ToughJetAdapter.ToughJetResponse>::class.java
                )
            } returns emptyArray()

            // when
            adapter.searchFlights(req)

            // then
            verify(exactly = 1) {
                restTemplate.postForObject(
                    any<String>(),
                    any(),
                    Array<ToughJetAdapter.ToughJetResponse>::class.java
                )
            }
            assertEquals("https://api.toughjet.com/flights", urlSlot.captured)
            assertEquals(expected, bodySlot.captured)
        }
    }

    @Test
    fun `maps ToughJetResponse array to Flight list`() {
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
                ToughJetAdapter.ToughJetResponse(
                    carrier = "Tough Jet",
                    basePrice = BigDecimal("100.00"),
                    tax = BigDecimal("20.00"),
                    discount = BigDecimal("10.00"),
                    departureAirportName = IATACode("BCN"),
                    arrivalAirportName = IATACode("MAD"),
                    outboundDateTime = Instant.parse("2025-01-20T10:15:00Z"),
                    inboundDateTime = Instant.parse("2025-01-20T18:30:00Z")
                )
            )
            val expected = listOf(
                Flight(
                    supplier = "ToughJet",
                    airline = "Tough Jet",
                    fare = BigDecimal("108.00"),
                    departureAirportCode = IATACode("BCN"),
                    destinationAirportCode = IATACode("MAD"),
                    departureDate = LocalDate.of(2025, 1, 20),
                    arrivalDate = LocalDate.of(2025, 1, 20)
                )
            )

            every {
                restTemplate.postForObject(
                    any<String>(),
                    any(),
                    Array<ToughJetAdapter.ToughJetResponse>::class.java
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
                origin = IATACode("BCN"),
                destination = IATACode("MAD"),
                departureDate = LocalDate.of(2025, 1, 20),
                returnDate = LocalDate.of(2025, 1, 28),
                numberOfPassengers = 1
            )
            every {
                restTemplate.postForObject(
                    any<String>(),
                    any(),
                    Array<ToughJetAdapter.ToughJetResponse>::class.java
                )
            } returns null

            // when
            val flights = adapter.searchFlights(request)

            // then
            assertTrue(flights.isEmpty())
        }
    }

    @Test
    fun `exposes supplier name ToughJet`() {
        // given

        // when
        val supplier = adapter.supplier()

        // then
        assertEquals("ToughJet", supplier)
    }
}
