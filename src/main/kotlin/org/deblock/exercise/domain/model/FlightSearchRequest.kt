package org.deblock.exercise.domain.model

import com.google.common.base.Preconditions.checkArgument
import java.time.LocalDate

data class FlightSearchRequest(
    val origin: IATACode,
    val destination: IATACode,
    val departureDate: LocalDate,
    val returnDate: LocalDate,
    val numberOfPassengers: Int,
) {
    init {
        checkArgument(numberOfPassengers >= 1 && numberOfPassengers <= 4, "Number of passengers must be between 1 and 4")
        checkArgument(departureDate.isBefore(returnDate), "Departure date must be before the return date")
    }
}