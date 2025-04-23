package com.sears.appointment.global

import com.sears.appointment.dto.ApiReturn
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<ApiReturn<Nothing>> {
        val apiResponse = ApiReturn<Nothing>(
            success = false,
            message = ex.message ?: "Bad request",
            data = null
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<ApiReturn<Map<String, String>>> {
        val errors = HashMap<String, String>()
        ex.bindingResult.allErrors.forEach { error ->
            val fieldName = (error as FieldError).field
            val errorMessage = error.defaultMessage ?: "Validation error"
            errors[fieldName] = errorMessage
        }
        
        val apiResponse = ApiReturn(
            success = false,
            message = "Validation failed",
            data = errors.toMap()
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ApiReturn<Nothing>> {
        val apiResponse = ApiReturn<Nothing>(
            success = false,
            message = "An unexpected error occurred: ${ex.message}",
            data = null
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse)
    }
} 