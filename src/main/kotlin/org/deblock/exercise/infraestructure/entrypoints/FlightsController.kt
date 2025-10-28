package org.deblock.exercise.infraestructure.entrypoints

import org.deblock.exercise.domain.model.Flight
import org.deblock.exercise.domain.model.FlightSearchRequest
import org.deblock.exercise.domain.port.inbound.FlightSupportUseCase
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/flights")
class FlightsController(
    val flightSupportUseCase: FlightSupportUseCase
) {

    @PostMapping
    fun flights(
        @RequestBody request: FlightSearchRequest
    ): ResponseEntity<List<Flight>> {

        return ResponseEntity.ok(flightSupportUseCase.searchFlights(request))
    }
}


