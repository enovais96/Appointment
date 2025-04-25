package com.sears.appointment.services

import com.sears.appointment.global.exceptions.ResourceNotFoundException
import com.sears.appointment.model.AvailabilitySlot
import com.sears.appointment.model.DayOfWeek
import com.sears.appointment.model.Doctor
import com.sears.appointment.model.DoctorAvailability
import com.sears.appointment.model.TimeSlot
import com.sears.appointment.repositories.DoctorAvailabilityRepository
import com.sears.appointment.repositories.DoctorRepository
import com.sears.appointment.services.interfaces.DoctorAvailabilityService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek as JavaDayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Optional

@Service
class DoctorAvailabilityServiceImpl(
    private val doctorAvailabilityRepository: DoctorAvailabilityRepository,
    private val doctorRepository: DoctorRepository
) : DoctorAvailabilityService {

    private val logger = LoggerFactory.getLogger(DoctorAvailabilityServiceImpl::class.java)
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val timeSlotDuration = 30 // minutes

    @Transactional
    override fun generateDoctorAvailability(
        doctorId: String, 
        fromDate: LocalDate, 
        toDate: LocalDate
    ): List<DoctorAvailability> {
        logger.info("Generating availability for doctor: {} from {} to {}", doctorId, fromDate, toDate)
        
        // Get the doctor to access their availability schedule
        val doctor = doctorRepository.findById(doctorId)
            .orElseThrow { ResourceNotFoundException("Doctor not found with ID: $doctorId") }
        
        // Delete any existing availability in the date range
        val existingAvailabilities = doctorAvailabilityRepository.findByDoctorId(doctorId)
            .filter { it.date in fromDate..toDate }
        
        if (existingAvailabilities.isNotEmpty()) {
            existingAvailabilities.forEach { 
                doctorAvailabilityRepository.delete(it)
            }
        }
        
        // Generate new availability for each date in the range
        val result = mutableListOf<DoctorAvailability>()
        var currentDate = fromDate
        
        while (currentDate <= toDate) {
            val javaDay = currentDate.dayOfWeek
            val doctorDay = mapJavaDayOfWeekToDoctorDayOfWeek(javaDay)
            
            // Find slots for this day of week in doctor's schedule
            val daySlots = doctor.availabilitySchedule.filter { it.dayOfWeek == doctorDay }
            
            if (daySlots.isNotEmpty()) {
                // Generate time slots for this day
                val timeSlots = generateTimeSlotsForDay(daySlots)
                
                // Create availability record
                val availability = DoctorAvailability(
                    doctorId = doctorId,
                    date = currentDate,
                    timeSlots = timeSlots
                )
                
                val saved = doctorAvailabilityRepository.save(availability)
                result.add(saved)
            }
            
            // Move to next day
            currentDate = currentDate.plusDays(1)
        }
        
        return result
    }

    override fun getDoctorAvailabilityByDate(doctorId: String, date: LocalDate): DoctorAvailability? {
        val availabilityOpt = doctorAvailabilityRepository.findByDoctorIdAndDate(doctorId, date)
        
        // If no availability exists yet for this date, check if we need to generate it
        if (availabilityOpt.isEmpty) {
            // Get the doctor
            val doctor = doctorRepository.findById(doctorId)
            if (doctor.isEmpty) {
                return null
            }
            
            // Get the day of week
            val javaDay = date.dayOfWeek
            val doctorDay = mapJavaDayOfWeekToDoctorDayOfWeek(javaDay)
            
            // Check if the doctor has any availability on this day of week
            val daySlots = doctor.get().availabilitySchedule.filter { it.dayOfWeek == doctorDay }
            
            if (daySlots.isNotEmpty()) {
                // Generate availability for this date
                val availabilities = generateDoctorAvailability(doctorId, date, date)
                if (availabilities.isNotEmpty()) {
                    return availabilities.first()
                }
            }
            
            return null
        }
        
        return availabilityOpt.get()
    }

    @Transactional
    override fun bookTimeSlot(
        doctorId: String, 
        date: LocalDate, 
        startTime: LocalTime, 
        appointmentId: String
    ): Boolean {
        logger.info("Booking time slot for doctor: {} on {} at {}", doctorId, date, startTime)
        
        // Get or generate availability for this date
        val availability = getDoctorAvailabilityByDate(doctorId, date) ?: return false
        
        // Find the matching time slot
        val startTimeString = startTime.format(timeFormatter)
        val endTimeString = startTime.plusMinutes(timeSlotDuration.toLong()).format(timeFormatter)
        
        // Get index of the time slot
        val timeSlotIndex = availability.timeSlots.indexOfFirst { 
            it.startTime == startTimeString && it.endTime == endTimeString && it.isAvailable 
        }
        
        if (timeSlotIndex == -1) {
            logger.error("No available time slot found for doctor: {} on {} at {}", doctorId, date, startTime)
            return false
        }
        
        // Update the time slot
        val updatedTimeSlots = availability.timeSlots.toMutableList()
        updatedTimeSlots[timeSlotIndex] = updatedTimeSlots[timeSlotIndex].copy(
            isAvailable = false,
            appointmentId = appointmentId
        )
        
        // Save the updated availability
        val updatedAvailability = availability.copy(
            timeSlots = updatedTimeSlots,
            updatedAt = System.currentTimeMillis()
        )
        
        doctorAvailabilityRepository.save(updatedAvailability)
        return true
    }

    @Transactional
    override fun releaseTimeSlot(doctorId: String, date: LocalDate, startTime: LocalTime): Boolean {
        logger.info("Releasing time slot for doctor: {} on {} at {}", doctorId, date, startTime)
        
        // Get availability for this date
        val availabilityOpt = doctorAvailabilityRepository.findByDoctorIdAndDate(doctorId, date)
        if (availabilityOpt.isEmpty) {
            return false
        }
        
        val availability = availabilityOpt.get()
        val startTimeString = startTime.format(timeFormatter)
        val endTimeString = startTime.plusMinutes(timeSlotDuration.toLong()).format(timeFormatter)
        
        // Find the time slot
        val timeSlotIndex = availability.timeSlots.indexOfFirst { 
            it.startTime == startTimeString && it.endTime == endTimeString && !it.isAvailable 
        }
        
        if (timeSlotIndex == -1) {
            return false
        }
        
        // Update the time slot
        val updatedTimeSlots = availability.timeSlots.toMutableList()
        updatedTimeSlots[timeSlotIndex] = updatedTimeSlots[timeSlotIndex].copy(
            isAvailable = true,
            appointmentId = null
        )
        
        // Save the updated availability
        val updatedAvailability = availability.copy(
            timeSlots = updatedTimeSlots,
            updatedAt = System.currentTimeMillis()
        )
        
        doctorAvailabilityRepository.save(updatedAvailability)
        return true
    }

    override fun getAvailableTimeSlots(doctorId: String, date: LocalDate): List<TimeSlot> {
        val availability = getDoctorAvailabilityByDate(doctorId, date)
        return availability?.timeSlots?.filter { it.isAvailable } ?: emptyList()
    }

    override fun findNextAvailableTimeSlot(
        doctorId: String, 
        fromDate: LocalDate, 
        fromTime: LocalTime
    ): Pair<LocalDate, String>? {
        // First, check the fromDate
        val availability = getDoctorAvailabilityByDate(doctorId, fromDate)
        
        if (availability != null) {
            val fromTimeString = fromTime.format(timeFormatter)
            
            // Find a time slot on the same day that starts after the fromTime
            val sameDay = availability.timeSlots
                .filter { it.isAvailable && it.startTime >= fromTimeString }
                .minByOrNull { it.startTime }
            
            if (sameDay != null) {
                return Pair(fromDate, sameDay.startTime)
            }
        }
        
        // Check future dates
        var currentDate = fromDate.plusDays(1)
        val maxDays = 30 // Limit search to 30 days in the future
        var daysSearched = 0
        
        while (daysSearched < maxDays) {
            val futureAvailability = getDoctorAvailabilityByDate(doctorId, currentDate)
            
            if (futureAvailability != null) {
                val firstAvailable = futureAvailability.timeSlots
                    .filter { it.isAvailable }
                    .minByOrNull { it.startTime }
                
                if (firstAvailable != null) {
                    return Pair(currentDate, firstAvailable.startTime)
                }
            }
            
            currentDate = currentDate.plusDays(1)
            daysSearched++
        }
        
        return null
    }

    override fun isTimeSlotAvailable(doctorId: String, date: LocalDate, time: LocalTime): Boolean {
        val startTimeString = time.format(timeFormatter)
        val endTimeString = time.plusMinutes(timeSlotDuration.toLong()).format(timeFormatter)
        
        val availability = getDoctorAvailabilityByDate(doctorId, date)
        
        if (availability == null) {
            // Check if the doctor works on this day
            val doctor = doctorRepository.findById(doctorId)
            if (doctor.isEmpty) {
                return false
            }
            
            // Get the day of week
            val javaDay = date.dayOfWeek
            val doctorDay = mapJavaDayOfWeekToDoctorDayOfWeek(javaDay)
            
            // Check if the doctor has any availability on this day of week
            val daySlots = doctor.get().availabilitySchedule.filter { it.dayOfWeek == doctorDay }
            
            if (daySlots.isEmpty()) {
                return false
            }
            
            // Check if the time falls within any of the doctor's availability slots
            val timeInRange = daySlots.any { slot ->
                val slotStart = LocalTime.parse(slot.startTime, timeFormatter)
                val slotEnd = LocalTime.parse(slot.endTime, timeFormatter)
                time >= slotStart && time.plusMinutes(timeSlotDuration.toLong()) <= slotEnd
            }
            
            return timeInRange && !hasConflictingAppointment(doctorId, date, time)
        }
        
        // Check if there is a matching time slot that is available
        return availability.timeSlots.any {
            it.startTime == startTimeString && it.endTime == endTimeString && it.isAvailable
        }
    }
    
    private fun generateTimeSlotsForDay(daySlots: List<AvailabilitySlot>): List<TimeSlot> {
        val timeSlots = mutableListOf<TimeSlot>()
        
        for (slot in daySlots) {
            var slotStartTime = LocalTime.parse(slot.startTime, timeFormatter)
            val slotEndTime = LocalTime.parse(slot.endTime, timeFormatter)
            
            // Generate 30-minute time slots
            while (slotStartTime.plusMinutes(timeSlotDuration.toLong()) <= slotEndTime) {
                val endTime = slotStartTime.plusMinutes(timeSlotDuration.toLong())
                
                timeSlots.add(
                    TimeSlot(
                        startTime = slotStartTime.format(timeFormatter),
                        endTime = endTime.format(timeFormatter),
                        isAvailable = true,
                        appointmentId = null
                    )
                )
                
                slotStartTime = endTime
            }
        }
        
        return timeSlots
    }
    
    private fun mapJavaDayOfWeekToDoctorDayOfWeek(javaDay: JavaDayOfWeek): DayOfWeek {
        return when (javaDay) {
            JavaDayOfWeek.MONDAY -> DayOfWeek.MONDAY
            JavaDayOfWeek.TUESDAY -> DayOfWeek.TUESDAY
            JavaDayOfWeek.WEDNESDAY -> DayOfWeek.WEDNESDAY
            JavaDayOfWeek.THURSDAY -> DayOfWeek.THURSDAY
            JavaDayOfWeek.FRIDAY -> DayOfWeek.FRIDAY
            JavaDayOfWeek.SATURDAY -> DayOfWeek.SATURDAY
            JavaDayOfWeek.SUNDAY -> DayOfWeek.SUNDAY
        }
    }
    
    private fun hasConflictingAppointment(doctorId: String, date: LocalDate, time: LocalTime): Boolean {
        // Check if there are any existing appointments in the appointment_solicitations collection
        // that overlap with this time slot
        
        // This is a simplification; in a real implementation you would query the appointment_solicitations collection
        // to check for conflicting appointments with the status CONFIRMED
        // Example query: find appointments where doctorId = doctorId, date = date, and time overlaps with the given time
        
        return false // Placeholder for actual implementation
    }
} 