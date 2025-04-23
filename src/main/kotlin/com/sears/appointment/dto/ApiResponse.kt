package com.sears.appointment.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Standard API response wrapper")
data class ApiResponse<T>(
    @Schema(description = "Indicates if the operation was successful", example = "true")
    val success: Boolean,
    
    @Schema(description = "A message describing the result", example = "Operation completed successfully")
    val message: String,
    
    @Schema(description = "The data returned by the operation, null if operation failed")
    val data: T? = null
) 