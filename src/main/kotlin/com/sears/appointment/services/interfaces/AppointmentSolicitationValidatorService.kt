package com.sears.appointment.services.interfaces

import com.sears.appointment.dto.AppointmentSolicitationResponseDto
import java.time.LocalDate
import java.time.LocalTime

interface AppointmentSolicitationValidatorService {
    /**
     * Validates and processes an appointment solicitation.
     * This includes checking doctor availability and scheduling the appointment if possible.
     *
     * @param solicitationId ID of the appointment solicitation to validate
     * @return Information about the processed appointment solicitation
     */
    fun validateAndProcessSolicitation(solicitationId: String): AppointmentSolicitationResponseDto
    
    /**
     * Confirms an appointment by assigning a doctor.
     * 
     * @param solicitationId ID of the appointment solicitation to confirm
     * @param doctorId ID of the doctor to assign to the appointment
     * @return Information about the confirmed appointment
     */
    fun confirmAppointment(solicitationId: String, doctorId: String): AppointmentSolicitationResponseDto
    
    /**
     * Suggests an alternative appointment time when the requested time is not available.
     * 
     * @param solicitationId ID of the appointment solicitation
     * @param suggestedDate Suggested alternative date
     * @param suggestedTime Suggested alternative time
     * @param doctorId ID of the doctor suggested for the appointment
     * @return Information about the appointment solicitation with a suggested time
     */
    fun suggestAlternativeAppointment(
        solicitationId: String, 
        suggestedDate: LocalDate,
        suggestedTime: String,
        doctorId: String
    ): AppointmentSolicitationResponseDto
    
    /**
     * Reprocesses a failed appointment solicitation by marking it as PENDING
     * and sending it back to the Kafka queue for another attempt.
     *
     * @param solicitationId ID of the appointment solicitation to reprocess
     */
    fun reprocessSolicitation(solicitationId: String)
    
    /**
     * Finds all doctors with a specific specialty.
     * 
     * @param specialty The medical specialty to find doctors for
     * @return List of doctor IDs with the specified specialty
     */
    fun findDoctorsWithSpecialty(specialty: String): List<String>
    
    /**
     * Checks if a doctor is available at the specified date and time.
     * 
     * @param doctorId ID of the doctor to check
     * @param date The date to check
     * @param time The time to check
     * @return true if the doctor is available, false otherwise
     */
    fun isDoctorAvailableAt(doctorId: String, date: LocalDate, time: LocalTime): Boolean
    
    /**
     * Finds the next available time slot for any doctor with the specified specialty.
     * 
     * @param specialty The medical specialty to find an available doctor for
     * @param fromDate The date to start searching from
     * @param fromTime The time to start searching from
     * @return A triple containing the doctor ID, date, and time of the next available slot, or null if none found
     */
    fun findNextAvailableTimeSlot(
        specialty: String, 
        fromDate: LocalDate, 
        fromTime: LocalTime
    ): Triple<String, LocalDate, String>?
} 