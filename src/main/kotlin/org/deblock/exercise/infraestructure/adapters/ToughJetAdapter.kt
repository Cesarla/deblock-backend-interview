package org.deblock.exercise.infraestructure.adapters

import dev.failsafe.CircuitBreaker
import dev.failsafe.Failsafe
import org.deblock.exercise.domain.model.Flight
import org.deblock.exercise.domain.model.FlightSearchRequest
import org.deblock.exercise.domain.model.IATACode
import org.deblock.exercise.domain.port.outbound.FlightSupplierPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.math.BigDecimal
import java.math.BigDecimal.ONE
import java.net.URI
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset.UTC

@Component
class ToughJetAdapter(
    private val restTemplate: RestTemplate = RestTemplate(),
    private val uri : URI = URI.create("https://api.toughjet.com/flights")
) : FlightSupplierPort {

    private val logger = LoggerFactory.getLogger(FlightSupplierPort::class.java)

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
            logger.error("Failed to search flights from ToughJet", e)
            emptyList()
        }
    }

    fun performSearch(request: FlightSearchRequest): List<Flight> {
        val request = ToughJetRequest(
            request.origin,
            request.destination,
            request.departureDate,
            request.returnDate,
            request.numberOfPassengers
        )

        val response = restTemplate.postForObject(uri, request, Array<ToughJetResponse>::class.java)
        return response?.map { it.toFlight() } ?: emptyList()
    }

    private fun ToughJetResponse.toFlight(): Flight =
        Flight(
            supplier = supplier(),
            airline = carrier,
            fare = basePrice
                .add(basePrice.times(tax.divide(BigDecimal.valueOf(100))))
                .multiply(ONE.subtract(discount.divide(BigDecimal.valueOf(100.0))))
                .setScale(2),
            departureAirportCode = departureAirportName,
            destinationAirportCode = arrivalAirportName,
            departureDate = outboundDateTime.atZone(UTC).toLocalDate(),
            arrivalDate = inboundDateTime.atZone(UTC).toLocalDate(),
        )

    data class ToughJetRequest(
        val from: IATACode,
        val to: IATACode,
        val inboundDate: LocalDate,
        val outboundDate: LocalDate,
        val numberOfAdults: Int
    )

    data class ToughJetResponse(
        val carrier: String,
        val basePrice: BigDecimal,
        val tax: BigDecimal,
        val discount: BigDecimal,
        val departureAirportName: IATACode,
        val arrivalAirportName: IATACode,
        val outboundDateTime: Instant,
        val inboundDateTime: Instant
    )

    override fun supplier(): String = "ToughJet"
}