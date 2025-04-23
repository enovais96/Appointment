package com.sears.appointment.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

@Schema(description = "Login request data")
data class LoginRequestDto(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email should be valid")
    @Schema(description = "User's email address", example = "user@example.com", required = true)
    val email: String,
    
    @field:NotBlank(message = "Password is required")
    @Schema(description = "User's password", example = "Password1!", required = true)
    val password: String
) 