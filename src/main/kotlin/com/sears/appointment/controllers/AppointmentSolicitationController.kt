package com.sears.appointment.controllers

import com.sears.appointment.dto.ApiReturn
import com.sears.appointment.dto.AppointmentSolicitationRequestDto
import com.sears.appointment.dto.AppointmentSolicitationResponseDto
import com.sears.appointment.services.interfaces.AppointmentSolicitationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/appointments")
@Tag(name = "4 - Appointment Solicitation", description = "Endpoints for managing appointment solicitations")
@SecurityRequirement(name = "Bearer")
class AppointmentSolicitationController(
    private val appointmentSolicitationService: AppointmentSolicitationService
) {

    @PostMapping("/solicitations")
    @Operation(
        summary = "Create appointment solicitation",
        description = "Creates a new appointment solicitation and sends it to processing queue"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Appointment solicitation created successfully",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = ApiReturn::class)
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request data or no doctors available with the requested specialty",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = ApiReturn::class)
                )]
            )
        ]
    )
    fun createAppointmentSolicitation(
        @Valid @RequestBody appointmentSolicitationRequestDto: AppointmentSolicitationRequestDto
    ): ResponseEntity<ApiReturn<AppointmentSolicitationResponseDto>> {
        val createdSolicitation = appointmentSolicitationService.createAppointmentSolicitation(
            appointmentSolicitationRequestDto
        )
        
        val apiResponse = ApiReturn(
            success = true,
            message = "Appointment solicitation created and sent for processing",
            data = createdSolicitation
        )
        
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse)
    }

    @GetMapping("/solicitations/{id}")
    @Operation(
        summary = "Get appointment solicitation by ID",
        description = "Retrieves an appointment solicitation by its ID"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Appointment solicitation retrieved successfully",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = ApiReturn::class)
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Appointment solicitation not found",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = ApiReturn::class)
                )]
            )
        ]
    )
    fun getAppointmentSolicitationById(
        @PathVariable id: String
    ): ResponseEntity<ApiReturn<AppointmentSolicitationResponseDto>> {
        val solicitation = appointmentSolicitationService.getAppointmentSolicitationById(id)
        
        val apiResponse = ApiReturn(
            success = true,
            message = "Appointment solicitation retrieved successfully",
            data = solicitation
        )
        
        return ResponseEntity.ok(apiResponse)
    }
} 