package com.sears.appointment.controllers

import com.sears.appointment.dto.ApiResponse
import com.sears.appointment.dto.UserRegistrationDto
import com.sears.appointment.dto.UserResponseDto
import com.sears.appointment.services.interfaces.UserService
import com.sears.appointment.utils.ResponseFactory
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
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
            SwaggerApiResponse(
                responseCode = "201",
                description = "User registered successfully",
                content = [Content(schema = Schema(implementation = ApiResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "400", 
                description = "Invalid input or email already exists",
                content = [Content(schema = Schema(implementation = ApiResponse::class))]
            ),
            SwaggerApiResponse(
                responseCode = "500",
                description = "Internal server error",
                content = [Content(schema = Schema(implementation = ApiResponse::class))]
            )
        ]
    )
    @PostMapping("/register", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun registerUser(@Valid @RequestBody userRegistrationDto: UserRegistrationDto): ResponseEntity<ApiResponse<UserResponseDto>> {
        try {
            val registeredUser = userService.registerUser(userRegistrationDto)
            return ResponseFactory.created(registeredUser, "User registered successfully")
        } catch (e: Exception) {
            throw e
        }
    }
} 