package org.deblock.exercise.infraestructure.adapters

import org.deblock.exercise.domain.model.Flight
import org.deblock.exercise.domain.model.FlightSearchRequest
import org.deblock.exercise.domain.model.IATACode
import org.deblock.exercise.domain.port.outbound.FlightSupplierPort
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

class CrazyAirAdapter  (
    private val restTemplate: RestTemplate = RestTemplate()
) : FlightSupplierPort {
    override suspend fun searchFlights(request: FlightSearchRequest): List<Flight> {

        val request = CrazyAirRequest(
            request.origin,
            request.destination,
            request.departureDate,
            request.returnDate,
            request.numberOfPassengers
        )
        val url = UriComponentsBuilder
            .fromHttpUrl("https://api.crazyair.com/flights")
            .toUriString()

        val response = restTemplate.postForObject(url, request, Array<CrazyAirResponse>::class.java)
        return response?.map { it.toFlight() } ?: emptyList()
    }

    private fun CrazyAirResponse.toFlight(): Flight =
        Flight(
            supplier = supplier(),
            airline = airline,
            fare = price.setScale(2),
            departureAirportCode = departureAirportCode,
            destinationAirportCode = destinationAirportCode,
            departureDate = departureDate.toLocalDate(),
            arrivalDate = arrivalDate.toLocalDate()
        )

    data class CrazyAirRequest(
        val origin: IATACode,
        val destination: IATACode,
        val departureDate: LocalDate,
        val returnDate: LocalDate,
        val passengerCount: Int
    )
    data class CrazyAirResponse(
        val airline: String,
        val price: BigDecimal,
        val departureAirportCode: IATACode,
        val destinationAirportCode: IATACode,
        val departureDate: LocalDateTime,
        val arrivalDate: LocalDateTime
    )

    override fun supplier(): String = "CrazyAir"
}