package com.sears.appointment.services

import com.sears.appointment.dto.AppointmentSolicitationResponseDto
import com.sears.appointment.global.exceptions.BadRequestException
import com.sears.appointment.model.AppointmentSolicitation
import com.sears.appointment.model.AppointmentStatus
import com.sears.appointment.repositories.AppointmentSolicitationRepository
import com.sears.appointment.repositories.DoctorRepository
import com.sears.appointment.services.interfaces.AppointmentSolicitationValidatorService
import com.sears.appointment.services.interfaces.KafkaProducerService
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.time.LocalDate

@ExtendWith(MockKExtension::class)
class AppointmentSolicitationServiceTest {

    @MockK
    private lateinit var appointmentSolicitationRepository: AppointmentSolicitationRepository

    @MockK
    private lateinit var doctorRepository: DoctorRepository

    @MockK
    private lateinit var kafkaProducerService: KafkaProducerService

    @MockK
    private lateinit var appointmentSolicitationValidatorService: AppointmentSolicitationValidatorService

    @InjectMockKs
    private lateinit var appointmentSolicitationService: AppointmentSolicitationServiceImpl

    private lateinit var mockAppointmentSolicitation: AppointmentSolicitation
    private lateinit var mockAppointmentSolicitationResponse: AppointmentSolicitationResponseDto

    @BeforeEach
    fun setUp() {
        // Set up mock objects used across tests
        mockAppointmentSolicitation = AppointmentSolicitation(
            id = "test-id",
            patientName = "John Doe",
            patientAge = 35,
            patientPhone = "123-456-7890",
            patientEmail = "john@example.com",
            specialty = "Orthopedics",
            requestedDate = LocalDate.now(),
            requestedTime = "09:00",
            status = AppointmentStatus.SUGGESTED,
            doctorId = "doctor-id",
            suggestedDate = LocalDate.now().plusDays(1),
            suggestedTime = "14:00",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        mockAppointmentSolicitationResponse = AppointmentSolicitationResponseDto(
            id = "test-id",
            patientName = "John Doe",
            patientAge = 35,
            patientPhone = "123-456-7890",
            patientEmail = "john@example.com",
            specialty = "Orthopedics",
            requestedDate = LocalDate.now(),
            requestedTime = "09:00",
            status = AppointmentStatus.SUGGESTED,
            doctorId = "doctor-id",
            suggestedDate = LocalDate.now().plusDays(1),
            suggestedTime = "14:00",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }

    @Test
    fun `getSuggestedAppointments should return paginated suggested appointments`() {
        // Arrange
        val pageable = PageRequest.of(0, 10, Sort.by("updatedAt").descending())
        val mockPage = PageImpl(listOf(mockAppointmentSolicitation), pageable, 1)
        
        every { 
            appointmentSolicitationRepository.findByStatus(AppointmentStatus.SUGGESTED, pageable) 
        } returns mockPage

        // Act
        val result = appointmentSolicitationService.getSuggestedAppointments(pageable)

        // Assert
        assertEquals(1, result.totalElements)
        assertEquals(mockAppointmentSolicitation.id, result.content[0].id)
        assertEquals(AppointmentStatus.SUGGESTED, result.content[0].status)
        
        verify(exactly = 1) { 
            appointmentSolicitationRepository.findByStatus(AppointmentStatus.SUGGESTED, pageable) 
        }
    }

    @Test
    fun `confirmSuggestedAppointment should confirm appointment when accept is true`() {
        // Arrange
        val id = "test-id"
        val accept = true
        
        every { 
            appointmentSolicitationRepository.findByIdAndStatus(id, AppointmentStatus.SUGGESTED) 
        } returns mockAppointmentSolicitation
        
        every { 
            appointmentSolicitationValidatorService.confirmAppointment(id, "doctor-id") 
        } returns mockAppointmentSolicitationResponse.copy(status = AppointmentStatus.CONFIRMED)

        // Act
        val result = appointmentSolicitationService.confirmSuggestedAppointment(id, accept)

        // Assert
        assertEquals(id, result.id)
        assertEquals(AppointmentStatus.CONFIRMED, result.status)
        
        verify(exactly = 1) { 
            appointmentSolicitationRepository.findByIdAndStatus(id, AppointmentStatus.SUGGESTED) 
        }
        verify(exactly = 1) { 
            appointmentSolicitationValidatorService.confirmAppointment(id, "doctor-id") 
        }
    }

    @Test
    fun `confirmSuggestedAppointment should reject appointment when accept is false`() {
        // Arrange
        val id = "test-id"
        val accept = false
        
        every { 
            appointmentSolicitationRepository.findByIdAndStatus(id, AppointmentStatus.SUGGESTED) 
        } returns mockAppointmentSolicitation
        
        val rejectedAppointment = mockAppointmentSolicitation.copy(
            status = AppointmentStatus.REJECTED,
            updatedAt = any()
        )
        
        every { 
            appointmentSolicitationRepository.save(match { 
                it.status == AppointmentStatus.REJECTED && it.id == id 
            }) 
        } returns rejectedAppointment

        // Act
        val result = appointmentSolicitationService.confirmSuggestedAppointment(id, accept)

        // Assert
        assertEquals(id, result.id)
        assertEquals(AppointmentStatus.REJECTED, result.status)
        
        verify(exactly = 1) { 
            appointmentSolicitationRepository.findByIdAndStatus(id, AppointmentStatus.SUGGESTED) 
        }
        verify(exactly = 1) { 
            appointmentSolicitationRepository.save(any()) 
        }
        verify(exactly = 0) { 
            appointmentSolicitationValidatorService.confirmAppointment(any(), any()) 
        }
    }

    @Test
    fun `confirmSuggestedAppointment should throw exception when appointment not found with SUGGESTED status`() {
        // Arrange
        val id = "test-id"
        val accept = true
        
        every { 
            appointmentSolicitationRepository.findByIdAndStatus(id, AppointmentStatus.SUGGESTED) 
        } returns null

        // Act & Assert
        val exception = assertThrows<BadRequestException> {
            appointmentSolicitationService.confirmSuggestedAppointment(id, accept)
        }
        
        assertEquals("No suggested appointment found with id: $id", exception.message)
        
        verify(exactly = 1) { 
            appointmentSolicitationRepository.findByIdAndStatus(id, AppointmentStatus.SUGGESTED) 
        }
        verify(exactly = 0) { 
            appointmentSolicitationValidatorService.confirmAppointment(any(), any()) 
        }
        verify(exactly = 0) { 
            appointmentSolicitationRepository.save(any()) 
        }
    }

    @Test
    fun `confirmSuggestedAppointment should throw exception when suggested date is null`() {
        // Arrange
        val id = "test-id"
        val accept = true
        
        val appointmentWithoutSuggestedDate = mockAppointmentSolicitation.copy(suggestedDate = null)
        
        every { 
            appointmentSolicitationRepository.findByIdAndStatus(id, AppointmentStatus.SUGGESTED) 
        } returns appointmentWithoutSuggestedDate

        // Act & Assert
        val exception = assertThrows<BadRequestException> {
            appointmentSolicitationService.confirmSuggestedAppointment(id, accept)
        }
        
        assertEquals("Suggested appointment has no suggested date", exception.message)
    }

    @Test
    fun `confirmSuggestedAppointment should throw exception when suggested time is null`() {
        // Arrange
        val id = "test-id"
        val accept = true
        
        val appointmentWithoutSuggestedTime = mockAppointmentSolicitation.copy(suggestedTime = null)
        
        every { 
            appointmentSolicitationRepository.findByIdAndStatus(id, AppointmentStatus.SUGGESTED) 
        } returns appointmentWithoutSuggestedTime

        // Act & Assert
        val exception = assertThrows<BadRequestException> {
            appointmentSolicitationService.confirmSuggestedAppointment(id, accept)
        }
        
        assertEquals("Suggested appointment has no suggested time", exception.message)
    }

    @Test
    fun `confirmSuggestedAppointment should throw exception when doctor ID is null`() {
        // Arrange
        val id = "test-id"
        val accept = true
        
        val appointmentWithoutDoctorId = mockAppointmentSolicitation.copy(doctorId = null)
        
        every { 
            appointmentSolicitationRepository.findByIdAndStatus(id, AppointmentStatus.SUGGESTED) 
        } returns appointmentWithoutDoctorId

        // Act & Assert
        val exception = assertThrows<BadRequestException> {
            appointmentSolicitationService.confirmSuggestedAppointment(id, accept)
        }
        
        assertEquals("Suggested appointment has no assigned doctor", exception.message)
    }
} 