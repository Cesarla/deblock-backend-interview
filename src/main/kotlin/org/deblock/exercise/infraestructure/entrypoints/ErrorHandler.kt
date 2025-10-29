package org.deblock.exercise.infraestructure.entrypoints

import dev.failsafe.CircuitBreakerOpenException
import dev.failsafe.TimeoutExceededException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ErrorHandler {

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleArgumentStateException(ex: IllegalArgumentException): ResponseEntity<Problem> {
        val response = Problem(HttpStatus.BAD_REQUEST, ex.message)
        return ResponseEntity.badRequest().body(response)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(ex: HttpMessageNotReadableException): ResponseEntity<Problem> {
        val response = Problem(HttpStatus.BAD_REQUEST, ex.mostSpecificCause.message)
        return ResponseEntity.badRequest().body(response)
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleHttpRequestMethodNotSupportedException(ex: HttpRequestMethodNotSupportedException): ResponseEntity<Problem> {
        val response = Problem(HttpStatus.METHOD_NOT_ALLOWED, ex.message)
        return ResponseEntity.status(response.status).body(response)
    }

    @ExceptionHandler(CircuitBreakerOpenException::class)
    fun handleCircuitBreakerOpen(ex: CircuitBreakerOpenException): ResponseEntity<Problem> {
        val response = Problem(
            HttpStatus.SERVICE_UNAVAILABLE,
            ex.message ?: "Service temporarily unavailable"
        )
        return ResponseEntity.status(503).body(response)
    }

    @ExceptionHandler(TimeoutExceededException::class)
    fun handleTimeout(ignored: TimeoutExceededException): ResponseEntity<Problem> {
        val response = Problem(
            HttpStatus.GATEWAY_TIMEOUT,
            "Request timeout"
        )
        return ResponseEntity.status(504).body(response)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<Problem> {
        val response = Problem(HttpStatus.INTERNAL_SERVER_ERROR, ex.message)
        return ResponseEntity.internalServerError().body(response)
    }
}