package org.deblock.exercise.domain.port.inbound

import org.deblock.exercise.domain.model.Flight
import org.deblock.exercise.domain.model.FlightSearchRequest

interface FlightSupportUseCase {

    fun searchFlights(request: FlightSearchRequest): List<Flight>

}