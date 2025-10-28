package org.deblock.exercise.infraestructure.adapters

import dev.failsafe.CircuitBreaker
import dev.failsafe.Failsafe
import org.deblock.exercise.domain.model.Flight
import org.deblock.exercise.domain.model.FlightSearchRequest
import org.deblock.exercise.domain.model.IATACode
import org.deblock.exercise.domain.port.outbound.FlightSupplierPort
import org.slf4j.LoggerFactory
import org.springframework.web.client.RestTemplate
import java.math.BigDecimal
import java.net.URI
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

class CrazyAirAdapter(
    private val restTemplate: RestTemplate = RestTemplate(),
    val uri: URI = URI.create("https://api.crazyair.com/flights")
) : FlightSupplierPort {

    private val logger = LoggerFactory.getLogger(CrazyAirAdapter::class.java)

    private val circuitBreaker = CircuitBreaker.builder<List<Flight>>()
        .withFailureThreshold(5, 10)
        .withSuccessThreshold(3, 10)
        .withDelay(Duration.ofSeconds(5))
        .build()

    override suspend fun searchFlights(request: FlightSearchRequest): List<Flight> {
        return try {
            Failsafe.with(circuitBreaker).get { ->
                performSearch(request)
            }
        } catch (e: Exception) {
            logger.error("Failed to search flights from CrazyAir", e)
            emptyList()
        }
    }

    fun performSearch(request: FlightSearchRequest): List<Flight> {
        val request = CrazyAirRequest(
            request.origin,
            request.destination,
            request.departureDate,
            request.returnDate,
            request.numberOfPassengers
        )

        val response = restTemplate.postForObject(uri, request, Array<CrazyAirResponse>::class.java)
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