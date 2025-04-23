package com.sears.appointment.utils

import com.sears.appointment.dto.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

object ResponseFactory {

    fun <T> success(data: T, message: String = "Operation completed successfully"): ResponseEntity<ApiResponse<T>> {
        val response = ApiResponse(
            success = true,
            message = message,
            data = data
        )
        return ResponseEntity.ok(response)
    }

    fun <T> created(data: T, message: String = "Resource created successfully"): ResponseEntity<ApiResponse<T>> {
        val response = ApiResponse(
            success = true,
            message = message,
            data = data
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    fun <T> error(message: String, status: HttpStatus = HttpStatus.BAD_REQUEST): ResponseEntity<ApiResponse<T>> {
        val response = ApiResponse<T>(
            success = false,
            message = message,
            data = null
        )
        return ResponseEntity.status(status).body(response)
    }
}
