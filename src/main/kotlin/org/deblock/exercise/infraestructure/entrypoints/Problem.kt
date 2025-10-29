package org.deblock.exercise.infraestructure.entrypoints

import org.springframework.http.HttpStatus

data class Problem(
    val title: String,
    val status: Int,
    val detail: String?
) {

    constructor(httpStatus: HttpStatus, detail: String?) : this(httpStatus.name, httpStatus.value(), detail)
}