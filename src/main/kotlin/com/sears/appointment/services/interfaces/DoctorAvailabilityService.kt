package com.sears.appointment.services.interfaces

import com.sears.appointment.model.DoctorAvailability
import com.sears.appointment.model.TimeSlot
import java.time.LocalDate
import java.time.LocalTime

interface DoctorAvailabilityService {

    fun generateDoctorAvailability(doctorId: String, fromDate: LocalDate, toDate: LocalDate): List<DoctorAvailability>
    fun getDoctorAvailabilityByDate(doctorId: String, date: LocalDate): DoctorAvailability?
    fun bookTimeSlot(doctorId: String, date: LocalDate, startTime: LocalTime, appointmentId: String): Boolean
    fun releaseTimeSlot(doctorId: String, date: LocalDate, startTime: LocalTime): Boolean
    fun getAvailableTimeSlots(doctorId: String, date: LocalDate): List<TimeSlot>
    fun findNextAvailableTimeSlotByDoctor(doctorId: String, fromDate: LocalDate, fromTime: LocalTime): Pair<LocalDate, String>?
    fun findNextAvailableTimeSlotBySpecialty(specialty: String, fromDate: LocalDate, fromTime: LocalTime): Triple<String, LocalDate, String>?
    fun isTimeSlotAvailable(doctorId: String, date: LocalDate, time: LocalTime): Boolean
    fun findDoctorsWithSpecialty(specialty: String): List<String>
    fun isDoctorAvailableAt(doctorId: String, date: LocalDate, time: LocalTime): Boolean
} 