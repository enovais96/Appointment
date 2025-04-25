package com.sears.appointment.services

import com.sears.appointment.dto.AppointmentSolicitationKafkaMessage
import com.sears.appointment.dto.AppointmentSolicitationRequestDto
import com.sears.appointment.dto.AppointmentSolicitationResponseDto
import com.sears.appointment.global.exceptions.BadRequestException
import com.sears.appointment.global.exceptions.ResourceNotFoundException
import com.sears.appointment.model.AppointmentSolicitation
import com.sears.appointment.model.AppointmentStatus
import com.sears.appointment.repositories.AppointmentSolicitationRepository
import com.sears.appointment.repositories.DoctorRepository
import com.sears.appointment.services.interfaces.AppointmentSolicitationService
import com.sears.appointment.services.interfaces.AppointmentSolicitationValidatorService
import com.sears.appointment.services.interfaces.DoctorAvailabilityService
import com.sears.appointment.services.interfaces.KafkaProducerService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Service
class AppointmentSolicitationServiceImpl(
    private val appointmentSolicitationRepository: AppointmentSolicitationRepository,
    private val doctorRepository: DoctorRepository,
    private val doctorAvailabilityService: DoctorAvailabilityService,
    private val kafkaProducerService: KafkaProducerService
) : AppointmentSolicitationService {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    @Transactional
    override fun createAppointmentSolicitation(
        appointmentSolicitationRequestDto: AppointmentSolicitationRequestDto
    ): AppointmentSolicitationResponseDto {
        val doctors = doctorRepository.findBySpecialty(appointmentSolicitationRequestDto.specialty)
        if (doctors.isEmpty()) {
            throw ResourceNotFoundException("No doctors found with specialty: ${appointmentSolicitationRequestDto.specialty}")
        }

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

        val kafkaMessage = AppointmentSolicitationKafkaMessage(
            appointmentSolicitationId = savedSolicitation.id!!
        )
        kafkaProducerService.sendAppointmentSolicitationMessage(kafkaMessage)

        return mapToResponseDto(savedSolicitation)
    }

    override fun getAppointmentSolicitationById(id: String): AppointmentSolicitationResponseDto {
        val appointmentSolicitation = appointmentSolicitationRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Appointment solicitation not found with id: $id") }
        
        return mapToResponseDto(appointmentSolicitation)
    }

    @Transactional(readOnly = true)
    override fun getSuggestedAppointments(pageable: Pageable): Page<AppointmentSolicitationResponseDto> {

        val suggestedAppointments = appointmentSolicitationRepository.findByStatus(
            AppointmentStatus.SUGGESTED, 
            pageable
        )
        
        return suggestedAppointments.map { this.mapToResponseDto(it) }
    }
    
    @Transactional
    override fun confirmSuggestedAppointment(id: String, accept: Boolean): AppointmentSolicitationResponseDto {

        val solicitation = appointmentSolicitationRepository.findByIdAndStatus(id, AppointmentStatus.SUGGESTED)
            ?: throw BadRequestException("No suggested appointment found with id: $id")
        
        if (accept) {

            val suggestedDate = solicitation.suggestedDate
                ?: throw BadRequestException("Suggested appointment has no suggested date")
            
            val suggestedTime = solicitation.suggestedTime
                ?: throw BadRequestException("Suggested appointment has no suggested time")
                
            val doctorId = solicitation.doctorId
                ?: throw BadRequestException("Suggested appointment has no assigned doctor")

            val confirmedSolicitation = solicitation.copy(
                status = AppointmentStatus.CONFIRMED,
                doctorId = doctorId,
                updatedAt = System.currentTimeMillis()
            )

            val saved = appointmentSolicitationRepository.save(confirmedSolicitation)
            return getAppointmentSolicitationById(saved.id!!)
            
        } else {

            val updatedSolicitation = solicitation.copy(
                status = AppointmentStatus.REJECTED,
                updatedAt = System.currentTimeMillis()
            )
            
            val savedSolicitation = appointmentSolicitationRepository.save(updatedSolicitation)
            return mapToResponseDto(savedSolicitation)
        }
    }

    @Transactional
    override fun confirmAppointment(solicitationId: String, doctorId: String): AppointmentSolicitationResponseDto {

        val solicitation = appointmentSolicitationRepository.findById(solicitationId)
            .orElseThrow { ResourceNotFoundException("Appointment solicitation not found with ID: $solicitationId") }

        val requestedTime = LocalTime.parse(solicitation.requestedTime, timeFormatter)
        val booked = doctorAvailabilityService.bookTimeSlot(
            doctorId,
            solicitation.requestedDate,
            requestedTime,
            solicitationId
        )

        if (!booked) {
            return getAppointmentSolicitationById(solicitationId)
        }

        val confirmedSolicitation = solicitation.copy(
            status = AppointmentStatus.CONFIRMED,
            doctorId = doctorId,
            updatedAt = System.currentTimeMillis()
        )

        val saved = appointmentSolicitationRepository.save(confirmedSolicitation)
        return getAppointmentSolicitationById(saved.id!!)
    }

    @Transactional
    override fun suggestAlternativeAppointment(
        solicitationId: String,
        suggestedDate: LocalDate,
        suggestedTime: String,
        doctorId: String
    ): AppointmentSolicitationResponseDto {

        val solicitation = appointmentSolicitationRepository.findById(solicitationId)
            .orElseThrow { ResourceNotFoundException("Appointment solicitation not found with ID: $solicitationId") }

        val suggestedTimeLocalTime = LocalTime.parse(suggestedTime, timeFormatter)
        val booked = doctorAvailabilityService.bookTimeSlot(
            doctorId,
            suggestedDate,
            suggestedTimeLocalTime,
            solicitationId
        )

        if (!booked) {
            return getAppointmentSolicitationById(solicitationId)
        }

        val suggestedSolicitation = solicitation.copy(
            status = AppointmentStatus.SUGGESTED,
            doctorId = doctorId,
            suggestedDate = suggestedDate,
            suggestedTime = suggestedTime.toString(),
            updatedAt = System.currentTimeMillis()
        )

        val saved = appointmentSolicitationRepository.save(suggestedSolicitation)
        return getAppointmentSolicitationById(saved.id!!)
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
            doctorId = appointmentSolicitation.doctorId,
            suggestedDate = appointmentSolicitation.suggestedDate,
            suggestedTime = appointmentSolicitation.suggestedTime,
            createdAt = appointmentSolicitation.createdAt,
            updatedAt = appointmentSolicitation.updatedAt
        )
    }
} 