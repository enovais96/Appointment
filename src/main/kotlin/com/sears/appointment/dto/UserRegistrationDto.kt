package com.sears.appointment.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

@Schema(description = "Data transfer object for user registration")
data class UserRegistrationDto(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email should be valid")
    @Schema(description = "User's email address", example = "user@example.com", required = true)
    val email: String,
    
    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, message = "Password must be at least 8 characters long")
    @field:Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character"
    )
    @Schema(
        description = "User's password - must contain at least one uppercase letter, one lowercase letter, one number, and one special character",
        example = "Password1!", 
        required = true
    )
    val password: String
) 