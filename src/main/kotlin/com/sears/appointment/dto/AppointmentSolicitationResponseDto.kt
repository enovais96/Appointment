package com.sears.appointment.dto

import com.sears.appointment.model.AppointmentStatus
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Pattern
import java.time.LocalDate
import java.time.LocalTime

@Schema(description = "Response data for appointment solicitation")
data class AppointmentSolicitationResponseDto(
    @Schema(description = "Unique identifier for the appointment solicitation", example = "61a2e0e3b54f6a23f0e66b4f")
    val id: String,
    
    @Schema(description = "Patient's full name", example = "John Doe")
    val patientName: String,
    
    @Schema(description = "Patient's age", example = "35")
    val patientAge: Int,
    
    @Schema(description = "Patient's phone number", example = "555-123-4567")
    val patientPhone: String,
    
    @Schema(description = "Patient's email address", example = "patient@example.com")
    val patientEmail: String,
    
    @Schema(description = "Requested medical specialty", example = "Orthopedics")
    val specialty: String,
    
    @Schema(description = "Requested appointment date", example = "2025-04-23")
    val requestedDate: LocalDate,
    
    @Schema(description = "Requested appointment time", example = "09:00")
    @field:Pattern(
        regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$",
        message = "Start time must be in format 'HH:mm' (e.g. '09:00' or '14:30')"
    )
    val requestedTime: String,
    
    @Schema(description = "Current status of the appointment solicitation", example = "PENDING")
    val status: AppointmentStatus,
    
    @Schema(description = "Creation timestamp in milliseconds", example = "1639057123456")
    val createdAt: Long
)