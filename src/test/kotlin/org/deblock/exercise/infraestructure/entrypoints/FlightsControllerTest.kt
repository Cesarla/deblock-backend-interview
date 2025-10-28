package org.deblock.exercise.infraestructure.entrypoints

import com.fasterxml.jackson.databind.ObjectMapper
import org.deblock.exercise.domain.model.Flight
import org.deblock.exercise.domain.model.FlightSearchRequest
import org.deblock.exercise.domain.model.IATACode
import org.deblock.exercise.domain.port.inbound.FlightSupportUseCase
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal
import java.time.LocalDate

@WebMvcTest(FlightsController::class)
class FlightsControllerTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val objectMapper: ObjectMapper
) {

    @MockBean
    lateinit var flightSupportUseCase: FlightSupportUseCase

    @Test
    fun `returns matched flights`() {
        // given
        val request = FlightSearchRequest(
            origin = IATACode("BCN"),
            destination = IATACode("MAD"),
            departureDate = LocalDate.of(2025, 5, 10),
            returnDate = LocalDate.of(2025, 5, 11),
            numberOfPassengers = 1
        )
        val flights = listOf(
            Flight(
                airline = "Vueling",
                supplier = "SupplierY",
                fare = BigDecimal.valueOf(41.00),
                departureAirportCode = IATACode("BCN"),
                destinationAirportCode = IATACode("MAD"),
                departureDate = LocalDate.of(2025, 5, 10),
                arrivalDate = LocalDate.of(2025, 5, 11)
            ),
            Flight(
                airline = "Iberia",
                supplier = "SupplierX",
                fare = BigDecimal.valueOf(150.00),
                departureAirportCode = IATACode("BCN"),
                destinationAirportCode = IATACode("MAD"),
                departureDate = LocalDate.of(2025, 5, 10),
                arrivalDate = LocalDate.of(2025, 5, 11)
            ),
        )
        given(flightSupportUseCase.searchFlights(request)).willReturn(flights)

        // when / then
        mockMvc.perform(
            post("/flights")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(flights.size))
            .andExpect(jsonPath("$[0].airline").value("Vueling"))
            .andExpect(jsonPath("$[0].fare").value(41))
            .andExpect(jsonPath("$[1].airline").value("Iberia"))
            .andExpect(jsonPath("$[1].fare").value(150))

        verify(flightSupportUseCase).searchFlights(request)

    }

    @Test
    fun `returns bad requests on validation errors`() {
        // given
        val request = """
            {
            "origin": "BCN",
            "destination": "MAD",
            "departureDate": "2025-05-10",
            "returnDate": "2025-05-09",
            "numberOfPassengers": 2
            }
        """.trimIndent()

        // when / then
        mockMvc.perform(
            post("/flights")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.title").value("BAD_REQUEST"))
            .andExpect(jsonPath("$.detail").value("Departure date must be before the return date"))

    }
}