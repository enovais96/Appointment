package com.sears.appointment.kafka

import com.sears.appointment.dto.AppointmentSolicitationKafkaMessage
import com.sears.appointment.dto.AppointmentSolicitationResponseDto
import com.sears.appointment.model.AppointmentStatus
import com.sears.appointment.services.interfaces.AppointmentSolicitationValidatorService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class AppointmentSolicitationConsumerTest {

    @Mock
    private lateinit var appointmentSolicitationValidatorService: AppointmentSolicitationValidatorService

    @InjectMocks
    private lateinit var appointmentSolicitationConsumer: AppointmentSolicitationConsumer

    @Test
    fun `when consumeAppointmentSolicitation with valid message then validate and process solicitation`() {
        // Arrange
        val solicitationId = "solicitation-123"
        val message = AppointmentSolicitationKafkaMessage(solicitationId)
        val responseDto = AppointmentSolicitationResponseDto(
            id = solicitationId,
            patientName = "John Doe",
            patientAge = 35,
            patientPhone = "555-123-4567",
            patientEmail = "john.doe@example.com",
            specialty = "Cardiology",
            requestedDate = LocalDate.now(),
            requestedTime = "09:00",
            status = AppointmentStatus.CONFIRMED,
            doctorId = "doctor-123",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        whenever(appointmentSolicitationValidatorService.validateAndProcessSolicitation(solicitationId))
            .thenReturn(responseDto)
        
        // Act
        appointmentSolicitationConsumer.consumeAppointmentSolicitation(message)
        
        // Assert
        verify(appointmentSolicitationValidatorService, times(1))
            .validateAndProcessSolicitation(solicitationId)
        verify(appointmentSolicitationValidatorService, never())
            .reprocessSolicitation(any())
    }

    @Test
    fun `when consumeAppointmentSolicitation with error then reprocess solicitation`() {
        // Arrange
        val solicitationId = "solicitation-123"
        val message = AppointmentSolicitationKafkaMessage(solicitationId)
        
        whenever(appointmentSolicitationValidatorService.validateAndProcessSolicitation(solicitationId))
            .thenThrow(RuntimeException("Processing error"))
        
        doNothing().whenever(appointmentSolicitationValidatorService)
            .reprocessSolicitation(solicitationId)
        
        // Act
        appointmentSolicitationConsumer.consumeAppointmentSolicitation(message)
        
        // Assert
        verify(appointmentSolicitationValidatorService, times(1))
            .validateAndProcessSolicitation(solicitationId)
        verify(appointmentSolicitationValidatorService, times(1))
            .reprocessSolicitation(solicitationId)
    }
} 