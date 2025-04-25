package com.sears.appointment.repositories

import com.sears.appointment.model.DoctorAvailability
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.Optional

@Repository
interface DoctorAvailabilityRepository : MongoRepository<DoctorAvailability, String> {
    /**
     * Finds doctor availability for a specific doctor on a specific date
     */
    fun findByDoctorIdAndDate(doctorId: String, date: LocalDate): Optional<DoctorAvailability>
    
    /**
     * Finds all availabilities for a specific doctor
     */
    fun findByDoctorId(doctorId: String): List<DoctorAvailability>
    
    /**
     * Finds all availabilities for a specific date
     */
    fun findByDate(date: LocalDate): List<DoctorAvailability>
    
    /**
     * Finds all availabilities for a date greater than or equal to the specified date
     */
    fun findByDateGreaterThanEqual(date: LocalDate): List<DoctorAvailability>
    
    /**
     * Deletes all availabilities for a specific doctor
     */
    fun deleteByDoctorId(doctorId: String)
} 