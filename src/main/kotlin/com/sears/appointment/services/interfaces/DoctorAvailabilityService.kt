package com.sears.appointment.services.interfaces

import com.sears.appointment.model.DoctorAvailability
import com.sears.appointment.model.TimeSlot
import java.time.LocalDate
import java.time.LocalTime

interface DoctorAvailabilityService {
    /**
     * Generates availability time slots for a doctor based on their schedule.
     * 
     * @param doctorId ID of the doctor
     * @param fromDate Start date to generate availability
     * @param toDate End date to generate availability
     * @return List of generated availability objects
     */
    fun generateDoctorAvailability(doctorId: String, fromDate: LocalDate, toDate: LocalDate): List<DoctorAvailability>
    
    /**
     * Gets the availability for a doctor on a specific date.
     * 
     * @param doctorId ID of the doctor
     * @param date The date to get availability for
     * @return The doctor's availability or null if not found
     */
    fun getDoctorAvailabilityByDate(doctorId: String, date: LocalDate): DoctorAvailability?
    
    /**
     * Books a time slot for a doctor.
     * 
     * @param doctorId ID of the doctor
     * @param date The date of the appointment
     * @param startTime The start time of the appointment
     * @param appointmentId ID of the appointment being booked
     * @return true if booking was successful, false otherwise
     */
    fun bookTimeSlot(doctorId: String, date: LocalDate, startTime: LocalTime, appointmentId: String): Boolean
    
    /**
     * Releases a previously booked time slot.
     * 
     * @param doctorId ID of the doctor
     * @param date The date of the appointment
     * @param startTime The start time of the appointment
     * @return true if release was successful, false otherwise
     */
    fun releaseTimeSlot(doctorId: String, date: LocalDate, startTime: LocalTime): Boolean
    
    /**
     * Gets all available time slots for a doctor on a specific date.
     * 
     * @param doctorId ID of the doctor
     * @param date The date to check
     * @return List of available time slots
     */
    fun getAvailableTimeSlots(doctorId: String, date: LocalDate): List<TimeSlot>
    
    /**
     * Finds the next available time slot for a specific doctor.
     * 
     * @param doctorId ID of the doctor
     * @param fromDate The date to start searching from
     * @param fromTime The time to start searching from
     * @return Pair of date and time for the next available slot, or null if none found
     */
    fun findNextAvailableTimeSlot(doctorId: String, fromDate: LocalDate, fromTime: LocalTime): Pair<LocalDate, String>?
    
    /**
     * Checks if a specific time slot is available for a doctor.
     * 
     * @param doctorId ID of the doctor
     * @param date The date to check
     * @param time The time to check
     * @return true if the time slot is available, false otherwise
     */
    fun isTimeSlotAvailable(doctorId: String, date: LocalDate, time: LocalTime): Boolean
} 