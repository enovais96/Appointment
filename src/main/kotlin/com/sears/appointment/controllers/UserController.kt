package com.sears.appointment.controllers

import com.sears.appointment.dto.ApiReturn
import com.sears.appointment.dto.LoginRequestDto
import com.sears.appointment.dto.RefreshTokenRequestDto
import com.sears.appointment.dto.TokenResponseDto
import com.sears.appointment.dto.UserRegistrationDto
import com.sears.appointment.dto.UserResponseDto
import com.sears.appointment.services.interfaces.UserService
import com.sears.appointment.utils.ResponseFactory
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "Endpoints for user registration and management")
class UserController(
    private val userService: UserService
) {

    @Operation(
        summary = "Register a new user",
        description = "Creates a new user account with the provided email and password"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "User registered successfully",
                content = [Content(schema = Schema(implementation = ApiReturn::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid input or email already exists",
                content = [Content(schema = Schema(implementation = ApiReturn::class))]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                content = [Content(schema = Schema(implementation = ApiReturn::class))]
            )
        ]
    )
    @PostMapping("/register", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun registerUser(@Valid @RequestBody userRegistrationDto: UserRegistrationDto): ResponseEntity<ApiReturn<UserResponseDto>> {
        try {
            val registeredUser = userService.registerUser(userRegistrationDto)
            return ResponseFactory.created(registeredUser, "User registered successfully")
        } catch (e: IllegalArgumentException) {
            return ResponseFactory.error(e.message ?: "Invalid request", HttpStatus.BAD_REQUEST)
        } catch (e: Exception) {
            return ResponseFactory.error("An unexpected error occurred: ${e.message}", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
    
    @Operation(
        summary = "User login",
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
        try {
            val tokenResponse = userService.loginUser(loginRequestDto)
            return ResponseFactory.success(tokenResponse, "Authentication successful")
        } catch (e: BadCredentialsException) {
            return ResponseFactory.error(e.message ?: "Invalid credentials", HttpStatus.UNAUTHORIZED)
        } catch (e: Exception) {
            return ResponseFactory.error("An unexpected error occurred: ${e.message}", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
    
    @Operation(
        summary = "Refresh authentication token",
        description = "Issues a new JWT token using a valid refresh token"
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
        try {
            val tokenResponse = userService.refreshToken(refreshTokenRequest.refreshToken)
            return ResponseFactory.success(tokenResponse, "Token refreshed successfully")
        } catch (e: IllegalArgumentException) {
            return ResponseFactory.error(e.message ?: "Invalid refresh token", HttpStatus.BAD_REQUEST)
        } catch (e: Exception) {
            return ResponseFactory.error("An unexpected error occurred: ${e.message}", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
} 