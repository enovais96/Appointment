package com.sears.appointment.services.interfaces

import com.sears.appointment.dto.AppointmentSolicitationRequestDto
import com.sears.appointment.dto.AppointmentSolicitationResponseDto

interface AppointmentSolicitationService {
    fun createAppointmentSolicitation(appointmentSolicitationRequestDto: AppointmentSolicitationRequestDto): AppointmentSolicitationResponseDto
    fun getAppointmentSolicitationById(id: String): AppointmentSolicitationResponseDto
} 