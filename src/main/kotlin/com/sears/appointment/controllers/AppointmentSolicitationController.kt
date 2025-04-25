package com.sears.appointment.controllers

import com.sears.appointment.dto.ApiReturn
import com.sears.appointment.dto.AppointmentSolicitationRequestDto
import com.sears.appointment.dto.AppointmentSolicitationResponseDto
import com.sears.appointment.dto.AppointmentSuggestionConfirmationDto
import com.sears.appointment.services.interfaces.AppointmentSolicitationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
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
    
    @GetMapping("/solicitations/suggested")
    @Operation(
        summary = "Get suggested appointments",
        description = "Returns all appointments with SUGGESTED status with pagination (10 per page)"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Suggested appointments retrieved successfully",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = ApiReturn::class)
                )]
            )
        ]
    )
    fun getSuggestedAppointments(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<ApiReturn<Page<AppointmentSolicitationResponseDto>>> {
        val pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending())
        val suggestedAppointments = appointmentSolicitationService.getSuggestedAppointments(pageable)
        
        val apiResponse = ApiReturn(
            success = true,
            message = "Suggested appointments retrieved successfully",
            data = suggestedAppointments
        )
        
        return ResponseEntity.ok(apiResponse)
    }
    
    @PostMapping("/solicitations/{id}/confirm")
    @Operation(
        summary = "Confirm suggested appointment",
        description = "Confirms or rejects a suggested appointment time"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Appointment confirmation processed successfully",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = ApiReturn::class)
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request or appointment not in SUGGESTED status",
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
    fun confirmSuggestedAppointment(
        @PathVariable("id") id: String,
        @Valid @RequestBody confirmationDto: AppointmentSuggestionConfirmationDto
    ): ResponseEntity<ApiReturn<AppointmentSolicitationResponseDto>> {
        val confirmedAppointment = appointmentSolicitationService.confirmSuggestedAppointment(
            id,
            confirmationDto.accept
        )
        
        val message = if (confirmationDto.accept) {
            "Appointment confirmed successfully"
        } else {
            "Appointment rejected successfully"
        }
        
        val apiResponse = ApiReturn(
            success = true,
            message = message,
            data = confirmedAppointment
        )
        
        return ResponseEntity.ok(apiResponse)
    }
} 