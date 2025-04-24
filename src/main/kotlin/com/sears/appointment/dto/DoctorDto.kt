package com.sears.appointment.dto

import com.sears.appointment.model.AvailabilitySlot
import com.sears.appointment.model.DayOfWeek
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Pattern

data class DoctorRequestDto(
    @field:NotBlank(message = "Name is required")
    val name: String,
    
    @field:NotBlank(message = "Specialty is required")
    val specialty: String,
    
    @field:NotEmpty(message = "Availability schedule is required")
    val availabilitySchedule: List<AvailabilitySlotDto>
)

data class DoctorResponseDto(
    val id: String,
    val name: String,
    val specialty: String,
    val availabilitySchedule: List<AvailabilitySlotDto>
)

data class AvailabilitySlotDto(
    val dayOfWeek: DayOfWeek,
    
    @field:Pattern(
        regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$",
        message = "Start time must be in format 'HH:mm' (e.g. '09:00' or '14:30')"
    )
    val startTime: String, // Format: "HH:mm"
    
    @field:Pattern(
        regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$",
        message = "End time must be in format 'HH:mm' (e.g. '09:00' or '17:00')"
    )
    val endTime: String    // Format: "HH:mm"
)

data class DoctorUpdateDto(
    val name: String? = null,
    val specialty: String? = null,
    val availabilitySchedule: List<AvailabilitySlotDto>? = null
) 