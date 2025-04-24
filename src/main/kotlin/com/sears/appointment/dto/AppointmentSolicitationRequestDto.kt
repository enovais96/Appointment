package com.sears.appointment.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import java.time.LocalDate
import java.time.LocalTime

@Schema(description = "Request data for creating a new appointment solicitation")
data class AppointmentSolicitationRequestDto(
    @Schema(description = "Patient's full name", example = "John Doe")
    @field:NotBlank(message = "Patient name is required")
    val patientName: String,
    
    @Schema(description = "Patient's age", example = "35")
    @field:Min(value = 0, message = "Age must be a positive number")
    val patientAge: Int,
    
    @Schema(description = "Patient's phone number", example = "555-123-4567")
    @field:NotBlank(message = "Phone is required")
    val patientPhone: String,
    
    @Schema(description = "Patient's email address", example = "patient@example.com")
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email should be valid")
    val patientEmail: String,
    
    @Schema(description = "Requested medical specialty", example = "Orthopedics")
    @field:NotBlank(message = "Specialty is required")
    val specialty: String,
    
    @Schema(description = "Requested appointment date", example = "2025-04-23")
    @field:NotNull(message = "Requested date is required")
    val requestedDate: LocalDate,
    
    @Schema(description = "Requested appointment time", example = "09:00")
    @field:NotNull(message = "Requested time is required")
    @field:Pattern(
        regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$",
        message = "Start time must be in format 'HH:mm' (e.g. '09:00' or '14:30')"
    )
    val requestedTime: String
) 