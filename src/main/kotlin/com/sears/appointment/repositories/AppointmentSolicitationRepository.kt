package com.sears.appointment.repositories

import com.sears.appointment.model.AppointmentSolicitation
import com.sears.appointment.model.AppointmentStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface AppointmentSolicitationRepository : MongoRepository<AppointmentSolicitation, String> {
    fun findByStatus(status: AppointmentStatus): List<AppointmentSolicitation>
    fun findByIdAndStatus(id: String, status: AppointmentStatus): AppointmentSolicitation?
    fun findByStatus(status: AppointmentStatus, pageable: Pageable): Page<AppointmentSolicitation>
} 