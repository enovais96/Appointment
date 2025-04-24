package com.sears.appointment.repositories

import com.sears.appointment.model.Doctor
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface DoctorRepository : MongoRepository<Doctor, String> {
    fun findBySpecialty(specialty: String): List<Doctor>
} 