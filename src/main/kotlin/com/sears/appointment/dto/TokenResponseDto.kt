package com.sears.appointment.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Authentication token response")
data class TokenResponseDto(
    @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    val accessToken: String,
    
    @Schema(description = "Refresh token for obtaining new access tokens", example = "8c7ec459-c0e2-4d13-a7a0-9c141617a3ea")
    val refreshToken: String,
    
    @Schema(description = "Token type", example = "Bearer")
    val tokenType: String = "Bearer"
) 