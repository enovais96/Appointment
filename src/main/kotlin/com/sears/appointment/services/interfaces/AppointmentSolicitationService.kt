package com.sears.appointment.services.interfaces

import com.sears.appointment.dto.AppointmentSolicitationRequestDto
import com.sears.appointment.dto.AppointmentSolicitationResponseDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDate

interface AppointmentSolicitationService {
    fun createAppointmentSolicitation(appointmentSolicitationRequestDto: AppointmentSolicitationRequestDto): AppointmentSolicitationResponseDto
    fun getAppointmentSolicitationById(id: String): AppointmentSolicitationResponseDto
    fun confirmAppointment(solicitationId: String, doctorId: String): AppointmentSolicitationResponseDto
    fun suggestAlternativeAppointment(solicitationId: String, suggestedDate: LocalDate, suggestedTime: String, doctorId: String): AppointmentSolicitationResponseDto
    fun getSuggestedAppointments(pageable: Pageable): Page<AppointmentSolicitationResponseDto>
    fun confirmSuggestedAppointment(id: String, accept: Boolean): AppointmentSolicitationResponseDto
} 