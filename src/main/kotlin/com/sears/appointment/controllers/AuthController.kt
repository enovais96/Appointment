package com.sears.appointment.controllers

import com.sears.appointment.dto.ApiReturn
import com.sears.appointment.dto.LoginRequestDto
import com.sears.appointment.dto.RefreshTokenRequestDto
import com.sears.appointment.dto.TokenResponseDto
import com.sears.appointment.services.interfaces.AuthService
import com.sears.appointment.utils.ResponseFactory
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
@Tag(name = "1 - Auth", description = "Endpoints for authentication")
class AuthController(
    private val authService: AuthService
) {
    @Operation(
        summary = "Login",
        description = "Authenticates a user and returns JWT and refresh tokens"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "User authenticated successfully",
                content = [Content(schema = Schema(implementation = ApiReturn::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Invalid credentials",
                content = [Content(schema = Schema(implementation = ApiReturn::class))]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                content = [Content(schema = Schema(implementation = ApiReturn::class))]
            )
        ]
    )
    @PostMapping("/login", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun loginUser(@Valid @RequestBody loginRequestDto: LoginRequestDto): ResponseEntity<ApiReturn<TokenResponseDto>> {
        val tokenResponse = authService.loginUser(loginRequestDto)
        return ResponseFactory.success(tokenResponse, "Authentication successful")
    }

    @Operation(
        summary = "Refresh authentication token",
        description = "Issues a new JWT token using a valid refresh token",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Token refreshed successfully",
                content = [Content(schema = Schema(implementation = ApiReturn::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid or expired refresh token",
                content = [Content(schema = Schema(implementation = ApiReturn::class))]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                content = [Content(schema = Schema(implementation = ApiReturn::class))]
            )
        ]
    )
    @PostMapping("/refresh-token", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun refreshToken(@Valid @RequestBody refreshTokenRequest: RefreshTokenRequestDto): ResponseEntity<ApiReturn<TokenResponseDto>> {
        val tokenResponse = authService.refreshToken(refreshTokenRequest.refreshToken)
        return ResponseFactory.success(tokenResponse, "Token refreshed successfully")
    }

}