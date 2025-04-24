package com.sears.appointment.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Pattern

@Document(collection = "doctors")
data class Doctor(
    @Id
    val id: String? = null,
    
    @field:NotBlank(message = "Name is required")
    val name: String,
    
    @field:NotBlank(message = "Specialty is required")
    val specialty: String,
    
    @field:NotEmpty(message = "Availability schedule is required")
    val availabilitySchedule: List<AvailabilitySlot>
)

data class AvailabilitySlot(
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

enum class DayOfWeek {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
} 