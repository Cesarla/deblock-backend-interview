package org.deblock.exercise.infraestructure.adapters

import org.deblock.exercise.domain.model.Flight
import org.deblock.exercise.domain.model.FlightSearchRequest
import org.deblock.exercise.domain.model.IATACode
import org.deblock.exercise.domain.port.outbound.FlightSupplierPort
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.math.BigDecimal
import java.math.BigDecimal.ONE
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset.UTC

@Component
class ToughJetAdapter(
    private val restTemplate: RestTemplate = RestTemplate()
) : FlightSupplierPort {
    override suspend fun searchFlights(request: FlightSearchRequest): List<Flight> {

        val request = ToughJetRequest(
            request.origin,
            request.destination,
            request.departureDate,
            request.returnDate,
            request.numberOfPassengers
        )
        val url = UriComponentsBuilder
            .fromHttpUrl("https://api.toughjet.com/flights")
            .toUriString()

        val response = restTemplate.postForObject(url, request, Array<ToughJetResponse>::class.java)
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