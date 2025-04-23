package com.sears.appointment.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "Request to refresh an authentication token")
data class RefreshTokenRequestDto(
    @field:NotBlank(message = "Refresh token is required")
    @Schema(description = "Refresh token obtained from previous login", example = "8c7ec459-c0e2-4d13-a7a0-9c141617a3ea", required = true)
    val refreshToken: String
) 