package com.sears.appointment.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import java.time.LocalDate
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

@Document(collection = "doctor_availabilities")
@CompoundIndexes(
    CompoundIndex(name = "doctor_date_idx", def = "{'doctorId': 1, 'date': 1}", unique = true)
)
data class DoctorAvailability(
    @Id
    val id: String? = null,
    
    @field:NotBlank(message = "Doctor ID is required")
    val doctorId: String,
    
    @field:NotNull(message = "Date is required")
    val date: LocalDate,
    
    val timeSlots: List<TimeSlot> = emptyList(),
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class TimeSlot(
    @field:NotBlank(message = "Start time is required")
    val startTime: String, // Format: "HH:mm"
    
    @field:NotBlank(message = "End time is required")
    val endTime: String, // Format: "HH:mm"
    
    val isAvailable: Boolean = true,
    
    // If booked, store the appointment ID
    val appointmentId: String? = null
) 