package org.deblock.exercise.domain.model

import com.google.common.base.Preconditions.checkArgument
import java.math.BigDecimal
import java.math.BigDecimal.ZERO
import java.time.LocalDate

data class Flight(
    val airline: String,
    val supplier: String,
    val fare: BigDecimal,
    val departureAirportCode: IATACode,
    val destinationAirportCode: IATACode,
    val departureDate: LocalDate,
    val arrivalDate: LocalDate,
) {
    init {
        checkArgument(!departureDate.isAfter(arrivalDate), "Departure date cannot be after the arrival date")
        checkArgument(fare.compareTo(ZERO) >= 0, "Fare must be bigger or equal than zero")
    }
}
