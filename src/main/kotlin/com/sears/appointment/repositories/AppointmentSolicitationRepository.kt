package com.sears.appointment.repositories

import com.sears.appointment.model.AppointmentSolicitation
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface AppointmentSolicitationRepository : MongoRepository<AppointmentSolicitation, String> 