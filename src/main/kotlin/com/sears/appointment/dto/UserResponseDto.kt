package com.sears.appointment.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Response data for user information")
data class UserResponseDto(
    @Schema(description = "User's unique identifier", example = "61a2e0e3b54f6a23f0e66b4f")
    val id: String?,
    
    @Schema(description = "User's email address", example = "user@example.com")
    val email: String
) 