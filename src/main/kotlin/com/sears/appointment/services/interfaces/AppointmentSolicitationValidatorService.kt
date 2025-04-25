package com.sears.appointment.services.interfaces

import com.sears.appointment.dto.AppointmentSolicitationResponseDto
import java.time.LocalDate
import java.time.LocalTime

interface AppointmentSolicitationValidatorService {

    fun validateAndProcessSolicitation(solicitationId: String): AppointmentSolicitationResponseDto
    fun reprocessSolicitation(solicitationId: String)

} 