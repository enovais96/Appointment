package com.sears.appointment.services

import com.sears.appointment.dto.AppointmentSolicitationKafkaMessage
import com.sears.appointment.dto.AppointmentSolicitationResponseDto
import com.sears.appointment.global.exceptions.ResourceNotFoundException
import com.sears.appointment.model.AppointmentSolicitation
import com.sears.appointment.model.AppointmentStatus
import com.sears.appointment.repositories.AppointmentSolicitationRepository
import com.sears.appointment.repositories.DoctorAvailabilityRepository
import com.sears.appointment.repositories.DoctorRepository
import com.sears.appointment.services.interfaces.AppointmentSolicitationService
import com.sears.appointment.services.interfaces.AppointmentSolicitationValidatorService
import com.sears.appointment.services.interfaces.DoctorAvailabilityService
import com.sears.appointment.services.interfaces.KafkaProducerService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Service
class AppointmentSolicitationValidatorServiceImpl(
    private val appointmentSolicitationRepository: AppointmentSolicitationRepository,
    private val doctorRepository: DoctorRepository,
    private val doctorAvailabilityRepository: DoctorAvailabilityRepository,
    private val doctorAvailabilityService: DoctorAvailabilityService,
    private val kafkaProducerService: KafkaProducerService,
    private val appointmentSolicitationService: AppointmentSolicitationService
) : AppointmentSolicitationValidatorService {

    private val logger = LoggerFactory.getLogger(AppointmentSolicitationValidatorServiceImpl::class.java)
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val timeSlotDuration = 30 // minutes

    @Transactional
    override fun validateAndProcessSolicitation(solicitationId: String): AppointmentSolicitationResponseDto {
        logger.info("Validating appointment solicitation with ID: {}", solicitationId)
        
        // Get the solicitation
        val solicitation = appointmentSolicitationRepository.findById(solicitationId)
            .orElseThrow { ResourceNotFoundException("Appointment solicitation not found with ID: $solicitationId") }
        
        // Check if solicitation is in PENDING status
        if (solicitation.status != AppointmentStatus.PENDING) {
            logger.info("Solicitation {} is not in PENDING status. Current status: {}", 
                solicitationId, solicitation.status)
            return appointmentSolicitationService.getAppointmentSolicitationById(solicitationId)
        }
        
        try {
            // Mark as PROCESSING
            val processingSolicitation = updateSolicitationStatus(solicitation, AppointmentStatus.PROCESSING)
            
            // Get the requested time as LocalTime for better handling
            val requestedTime = LocalTime.parse(processingSolicitation.requestedTime, timeFormatter)
            
            // Find doctors with the requested specialty
            val doctorsWithSpecialty = findDoctorsWithSpecialty(processingSolicitation.specialty)
            if (doctorsWithSpecialty.isEmpty()) {
                logger.error("No doctors found with specialty: {}", processingSolicitation.specialty)
                return updateSolicitationStatus(
                    processingSolicitation, 
                    AppointmentStatus.REJECTED
                ).let { appointmentSolicitationService.getAppointmentSolicitationById(it.id!!) }
            }
            
            // Check each doctor's availability
            var availableDoctorId: String? = null
            for (doctorId in doctorsWithSpecialty) {
                if (isDoctorAvailableAt(
                        doctorId, 
                        processingSolicitation.requestedDate, 
                        requestedTime
                    )) {
                    availableDoctorId = doctorId
                    break
                }
            }
            
            // If a doctor is available, confirm the appointment
            if (availableDoctorId != null) {
                logger.info("Doctor {} is available at the requested time", availableDoctorId)
                return confirmAppointment(processingSolicitation.id!!, availableDoctorId)
            }
            
            // If no doctor is available at the requested time, find the next available time
            logger.info("No doctor available at the requested time for solicitation: {}", solicitationId)
            
            val nextAvailableSlot = findNextAvailableTimeSlot(
                processingSolicitation.specialty,
                processingSolicitation.requestedDate,
                requestedTime
            )
            
            if (nextAvailableSlot != null) {
                val (suggestedDoctorId, suggestedDate, suggestedTime) = nextAvailableSlot
                
                logger.info("Found alternative time slot: doctor={}, date={}, time={}", 
                    suggestedDoctorId, suggestedDate, suggestedTime)
                
                // Suggest an alternative appointment time
                return suggestAlternativeAppointment(
                    processingSolicitation.id!!,
                    suggestedDate,
                    suggestedTime,
                    suggestedDoctorId
                )
            }
            
            // If no alternative time found, mark as REJECTED
            logger.error("No available time slots found for specialty: {}", processingSolicitation.specialty)
            val rejectedSolicitation = updateSolicitationStatus(processingSolicitation, AppointmentStatus.REJECTED)
            return appointmentSolicitationService.getAppointmentSolicitationById(rejectedSolicitation.id!!)
            
        } catch (ex: Exception) {
            logger.error("Error processing solicitation: {}", solicitationId, ex)
            throw ex
        }
    }
    
    @Transactional
    override fun confirmAppointment(solicitationId: String, doctorId: String): AppointmentSolicitationResponseDto {
        logger.info("Confirming appointment solicitation with ID: {} for doctor: {}", solicitationId, doctorId)
        
        val solicitation = appointmentSolicitationRepository.findById(solicitationId)
            .orElseThrow { ResourceNotFoundException("Appointment solicitation not found with ID: $solicitationId") }
        
        // Book the time slot for the doctor
        val requestedTime = LocalTime.parse(solicitation.requestedTime, timeFormatter)
        val booked = doctorAvailabilityService.bookTimeSlot(
            doctorId, 
            solicitation.requestedDate, 
            requestedTime, 
            solicitationId
        )
        
        if (!booked) {
            logger.error("Failed to book time slot for doctor: {}", doctorId)
            return appointmentSolicitationService.getAppointmentSolicitationById(solicitationId)
        }
        
        // Update the solicitation with the doctor and confirm it
        val confirmedSolicitation = solicitation.copy(
            status = AppointmentStatus.CONFIRMED,
            doctorId = doctorId,
            updatedAt = System.currentTimeMillis()
        )
        
        val saved = appointmentSolicitationRepository.save(confirmedSolicitation)
        return appointmentSolicitationService.getAppointmentSolicitationById(saved.id!!)
    }
    
    @Transactional
    override fun suggestAlternativeAppointment(
        solicitationId: String,
        suggestedDate: LocalDate,
        suggestedTime: String,
        doctorId: String
    ): AppointmentSolicitationResponseDto {
        logger.info("Suggesting alternative appointment for solicitation: {}", solicitationId)
        
        val solicitation = appointmentSolicitationRepository.findById(solicitationId)
            .orElseThrow { ResourceNotFoundException("Appointment solicitation not found with ID: $solicitationId") }
        
        // Update the solicitation with the suggested time and doctor
        val suggestedSolicitation = solicitation.copy(
            status = AppointmentStatus.SUGGESTED,
            doctorId = doctorId,
            suggestedDate = suggestedDate,
            suggestedTime = suggestedTime,
            updatedAt = System.currentTimeMillis()
        )
        
        val saved = appointmentSolicitationRepository.save(suggestedSolicitation)
        return appointmentSolicitationService.getAppointmentSolicitationById(saved.id!!)
    }
    
    @Transactional
    override fun reprocessSolicitation(solicitationId: String) {
        logger.info("Reprocessing solicitation: {}", solicitationId)
        
        try {
            // Get the solicitation
            val solicitation = appointmentSolicitationRepository.findById(solicitationId)
                .orElseThrow { ResourceNotFoundException("Appointment solicitation not found with ID: $solicitationId") }
            
            // Mark as PENDING again
            val pendingSolicitation = updateSolicitationStatus(solicitation, AppointmentStatus.PENDING)
            
            // Send back to Kafka
            val kafkaMessage = AppointmentSolicitationKafkaMessage(
                appointmentSolicitationId = pendingSolicitation.id!!
            )
            kafkaProducerService.sendAppointmentSolicitationMessage(kafkaMessage)
            
            logger.info("Solicitation {} marked as PENDING and sent back to Kafka", solicitationId)
        } catch (ex: Exception) {
            logger.error("Error reprocessing solicitation: {}", solicitationId, ex)
            throw ex
        }
    }
    
    override fun findDoctorsWithSpecialty(specialty: String): List<String> {
        return doctorRepository.findBySpecialty(specialty)
            .map { it.id!! }
    }
    
    override fun isDoctorAvailableAt(doctorId: String, date: LocalDate, time: LocalTime): Boolean {
        return doctorAvailabilityService.isTimeSlotAvailable(doctorId, date, time)
    }
    
    override fun findNextAvailableTimeSlot(
        specialty: String,
        fromDate: LocalDate,
        fromTime: LocalTime
    ): Triple<String, LocalDate, String>? {
        logger.info("Finding next available time slot for specialty: {}", specialty)
        
        // Get all doctors with the specialty
        val doctorsWithSpecialty = findDoctorsWithSpecialty(specialty)
        if (doctorsWithSpecialty.isEmpty()) {
            return null
        }
        
        // Check each doctor for the next available slot
        for (doctorId in doctorsWithSpecialty) {
            val nextAvailable = doctorAvailabilityService.findNextAvailableTimeSlot(doctorId, fromDate, fromTime)
            if (nextAvailable != null) {
                val (availableDate, availableTime) = nextAvailable
                return Triple(doctorId, availableDate, availableTime)
            }
        }
        
        return null
    }
    
    private fun updateSolicitationStatus(
        solicitation: AppointmentSolicitation,
        status: AppointmentStatus
    ): AppointmentSolicitation {
        val updatedSolicitation = solicitation.copy(
            status = status,
            updatedAt = System.currentTimeMillis()
        )
        return appointmentSolicitationRepository.save(updatedSolicitation)
    }
} 