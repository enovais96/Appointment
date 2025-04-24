package com.sears.appointment.services

import com.sears.appointment.dto.AppointmentSolicitationKafkaMessage
import com.sears.appointment.dto.AppointmentSolicitationRequestDto
import com.sears.appointment.dto.AppointmentSolicitationResponseDto
import com.sears.appointment.global.exceptions.ResourceNotFoundException
import com.sears.appointment.model.AppointmentSolicitation
import com.sears.appointment.repositories.AppointmentSolicitationRepository
import com.sears.appointment.repositories.DoctorRepository
import com.sears.appointment.services.interfaces.AppointmentSolicitationService
import com.sears.appointment.services.interfaces.KafkaProducerService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AppointmentSolicitationServiceImpl(
    private val appointmentSolicitationRepository: AppointmentSolicitationRepository,
    private val doctorRepository: DoctorRepository,
    private val kafkaProducerService: KafkaProducerService
) : AppointmentSolicitationService {

    @Transactional
    override fun createAppointmentSolicitation(
        appointmentSolicitationRequestDto: AppointmentSolicitationRequestDto
    ): AppointmentSolicitationResponseDto {
        // Check if there are doctors with the requested specialty
        val doctors = doctorRepository.findBySpecialty(appointmentSolicitationRequestDto.specialty)
        if (doctors.isEmpty()) {
            throw ResourceNotFoundException("No doctors found with specialty: ${appointmentSolicitationRequestDto.specialty}")
        }

        // Create and save the appointment solicitation
        val appointmentSolicitation = AppointmentSolicitation(
            patientName = appointmentSolicitationRequestDto.patientName,
            patientAge = appointmentSolicitationRequestDto.patientAge,
            patientPhone = appointmentSolicitationRequestDto.patientPhone,
            patientEmail = appointmentSolicitationRequestDto.patientEmail,
            specialty = appointmentSolicitationRequestDto.specialty,
            requestedDate = appointmentSolicitationRequestDto.requestedDate,
            requestedTime = appointmentSolicitationRequestDto.requestedTime
        )

        val savedSolicitation = appointmentSolicitationRepository.save(appointmentSolicitation)

        // Send the solicitation ID to Kafka for processing
        val kafkaMessage = AppointmentSolicitationKafkaMessage(
            appointmentSolicitationId = savedSolicitation.id!!
        )
        kafkaProducerService.sendAppointmentSolicitationMessage(kafkaMessage)

        // Return the response DTO
        return mapToResponseDto(savedSolicitation)
    }

    override fun getAppointmentSolicitationById(id: String): AppointmentSolicitationResponseDto {
        val appointmentSolicitation = appointmentSolicitationRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Appointment solicitation not found with id: $id") }
        
        return mapToResponseDto(appointmentSolicitation)
    }

    private fun mapToResponseDto(appointmentSolicitation: AppointmentSolicitation): AppointmentSolicitationResponseDto {
        return AppointmentSolicitationResponseDto(
            id = appointmentSolicitation.id!!,
            patientName = appointmentSolicitation.patientName,
            patientAge = appointmentSolicitation.patientAge,
            patientPhone = appointmentSolicitation.patientPhone,
            patientEmail = appointmentSolicitation.patientEmail,
            specialty = appointmentSolicitation.specialty,
            requestedDate = appointmentSolicitation.requestedDate,
            requestedTime = appointmentSolicitation.requestedTime,
            status = appointmentSolicitation.status,
            createdAt = appointmentSolicitation.createdAt
        )
    }
} 