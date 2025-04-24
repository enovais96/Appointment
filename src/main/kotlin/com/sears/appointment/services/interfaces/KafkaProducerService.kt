package com.sears.appointment.services.interfaces

import com.sears.appointment.dto.AppointmentSolicitationKafkaMessage

interface KafkaProducerService {
    fun sendAppointmentSolicitationMessage(message: AppointmentSolicitationKafkaMessage)
} 