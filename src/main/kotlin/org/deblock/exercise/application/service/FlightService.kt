package org.deblock.exercise.application.service

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.deblock.exercise.domain.model.Flight
import org.deblock.exercise.domain.model.FlightSearchRequest
import org.deblock.exercise.domain.port.inbound.FlightSupportUseCase
import org.deblock.exercise.domain.port.outbound.FlightSupplierPort
import org.springframework.stereotype.Service

@Service
class FlightService(
    private val suppliers: List<FlightSupplierPort>
) : FlightSupportUseCase {
    override fun searchFlights(request: FlightSearchRequest): List<Flight> = runBlocking {
        coroutineScope {
            suppliers
                .map { supplier ->
                    async { supplier.searchFlights(request) }
                }
                .awaitAll()
                .flatten()
                .sortedBy { it.fare }
        }
    }
}