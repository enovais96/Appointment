package com.sears.appointment.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate
import java.time.LocalTime
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Min

@Document(collection = "appointment_solicitations")
data class AppointmentSolicitation(
    @Id
    val id: String? = null,
    
    @field:NotBlank(message = "Patient name is required")
    val patientName: String,
    
    @field:Min(value = 0, message = "Age must be a positive number")
    val patientAge: Int,
    
    @field:NotBlank(message = "Phone is required")
    val patientPhone: String,
    
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email should be valid")
    val patientEmail: String,
    
    @field:NotBlank(message = "Specialty is required")
    val specialty: String,
    
    @field:NotNull(message = "Requested date is required")
    val requestedDate: LocalDate,
    
    @field:NotNull(message = "Requested time is required")
    val requestedTime: String,
    
    val status: AppointmentStatus = AppointmentStatus.PENDING,
    
    val createdAt: Long = System.currentTimeMillis()
)

enum class AppointmentStatus {
    PENDING, 
    PROCESSED, 
    SUGGESTED, 
    CONFIRMED, 
    REJECTED
} 