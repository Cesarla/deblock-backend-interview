package org.deblock.exercise.domain.port.outbound

import org.deblock.exercise.domain.model.Flight
import org.deblock.exercise.domain.model.FlightSearchRequest

interface FlightSupplierPort {

    suspend fun searchFlights(request: FlightSearchRequest): List<Flight>

    fun supplier(): String

}