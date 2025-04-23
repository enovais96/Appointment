package com.sears.appointment.controllers

import com.sears.appointment.dto.ApiReturn
import com.sears.appointment.dto.UserRegistrationDto
import com.sears.appointment.dto.UserResponseDto
import com.sears.appointment.services.interfaces.UserService
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
@RequestMapping("/api/users")
@Tag(name = "2 - User", description = "Endpoints for user registration and management")
class UserController(
    private val userService: UserService
) {

    @Operation(
        summary = "1 - Register a new user",
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
        val registeredUser = userService.registerUser(userRegistrationDto)
        return ResponseFactory.created(registeredUser, "User registered successfully")
    }
} 