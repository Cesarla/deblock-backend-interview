package org.deblock.exercise.domain.model

import com.google.common.base.Preconditions.*

@JvmInline
value class IATACode(val code: String) {

    init {
        checkArgument(regex.matches(code), "IATA CODE must match ^[A-Z0-9]{3}$")
    }

    companion object {
        val regex = Regex("^[A-Z0-9]{3}$")
    }
}