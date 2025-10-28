package org.deblock.exercise.application.service

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.deblock.exercise.domain.model.Flight
import org.deblock.exercise.domain.model.FlightSearchRequest
import org.deblock.exercise.domain.port.inbound.FlightSupportUseCase
import org.deblock.exercise.domain.port.outbound.FlightSupplierPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class FlightService(
    private val suppliers: List<FlightSupplierPort>
) : FlightSupportUseCase {

    private val logger = LoggerFactory.getLogger(FlightService::class.java)

    override fun searchFlights(request: FlightSearchRequest): List<Flight> = runBlocking {
        coroutineScope {
            suppliers
                .map { supplier ->
                    async {
                        runCatching {
                            supplier.searchFlights(request)
                        }.getOrElse { e ->
                            logger.error("Unexpected error while processing ${supplier.supplier()}", e)
                            emptyList()
                        }
                    }
                }
                .awaitAll()
                .flatten()
                .sortedBy { it.fare }
        }
    }
}