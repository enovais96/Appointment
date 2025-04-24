package com.sears.appointment.services

import com.sears.appointment.dto.AppointmentSolicitationKafkaMessage
import com.sears.appointment.dto.AppointmentSolicitationRequestDto
import com.sears.appointment.global.exceptions.ResourceNotFoundException
import com.sears.appointment.model.AppointmentSolicitation
import com.sears.appointment.model.AppointmentStatus
import com.sears.appointment.model.AvailabilitySlot
import com.sears.appointment.model.DayOfWeek
import com.sears.appointment.model.Doctor
import com.sears.appointment.repositories.AppointmentSolicitationRepository
import com.sears.appointment.repositories.DoctorRepository
import com.sears.appointment.services.interfaces.KafkaProducerService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.time.LocalDate
import java.time.LocalTime
import java.util.*
import org.junit.jupiter.api.Assertions.*

@ExtendWith(MockitoExtension::class)
class AppointmentSolicitationServiceImplTest {

    @Mock
    private lateinit var appointmentSolicitationRepository: AppointmentSolicitationRepository

    @Mock
    private lateinit var doctorRepository: DoctorRepository

    @Mock
    private lateinit var kafkaProducerService: KafkaProducerService

    @InjectMocks
    private lateinit var appointmentSolicitationService: AppointmentSolicitationServiceImpl

    private lateinit var appointmentSolicitationRequestDto: AppointmentSolicitationRequestDto
    private lateinit var appointmentSolicitation: AppointmentSolicitation
    private lateinit var doctor: Doctor

    @BeforeEach
    fun setUp() {
        appointmentSolicitationRequestDto = AppointmentSolicitationRequestDto(
            patientName = "John Doe",
            patientAge = 35,
            patientPhone = "555-123-4567",
            patientEmail = "john.doe@example.com",
            specialty = "Orthopedics",
            requestedDate = LocalDate.of(2025, 4, 23),
            requestedTime = LocalTime.of(9, 0)
        )

        appointmentSolicitation = AppointmentSolicitation(
            id = "123",
            patientName = "John Doe",
            patientAge = 35,
            patientPhone = "555-123-4567",
            patientEmail = "john.doe@example.com",
            specialty = "Orthopedics",
            requestedDate = LocalDate.of(2025, 4, 23),
            requestedTime = LocalTime.of(9, 0),
            status = AppointmentStatus.PENDING,
            createdAt = 1639057123456
        )

        doctor = Doctor(
            id = "456",
            name = "Dr. Smith",
            specialty = "Orthopedics",
            availabilitySchedule = listOf(
                AvailabilitySlot(
                    dayOfWeek = DayOfWeek.MONDAY,
                    startTime = "08:00",
                    endTime = "16:00"
                )
            )
        )
    }

    @Test
    fun `when createAppointmentSolicitation with valid data and specialty exists then create and return solicitation`() {
        // Arrange
        `when`(doctorRepository.findBySpecialty("Orthopedics")).thenReturn(listOf(doctor))
        `when`(appointmentSolicitationRepository.save(any())).thenReturn(appointmentSolicitation)
        doNothing().`when`(kafkaProducerService).sendAppointmentSolicitationMessage(any())

        // Act
        val result = appointmentSolicitationService.createAppointmentSolicitation(appointmentSolicitationRequestDto)

        // Assert
        assertEquals("123", result.id)
        assertEquals("John Doe", result.patientName)
        assertEquals(35, result.patientAge)
        assertEquals("Orthopedics", result.specialty)
        assertEquals(LocalDate.of(2025, 4, 23), result.requestedDate)
        assertEquals(LocalTime.of(9, 0), result.requestedTime)
        assertEquals(AppointmentStatus.PENDING, result.status)

        verify(doctorRepository, times(1)).findBySpecialty("Orthopedics")
        verify(appointmentSolicitationRepository, times(1)).save(any())
        verify(kafkaProducerService, times(1)).sendAppointmentSolicitationMessage(
            check<AppointmentSolicitationKafkaMessage> { message ->
                assertEquals("123", message.appointmentSolicitationId)
            }
        )
    }

    @Test
    fun `when createAppointmentSolicitation with non-existent specialty then throw ResourceNotFoundException`() {
        // Arrange
        `when`(doctorRepository.findBySpecialty("Orthopedics")).thenReturn(emptyList())

        // Act & Assert
        val exception = assertThrows(ResourceNotFoundException::class.java) {
            appointmentSolicitationService.createAppointmentSolicitation(appointmentSolicitationRequestDto)
        }

        assertEquals("No doctors found with specialty: Orthopedics", exception.message)
        verify(doctorRepository, times(1)).findBySpecialty("Orthopedics")
        verify(appointmentSolicitationRepository, never()).save(any())
        verify(kafkaProducerService, never()).sendAppointmentSolicitationMessage(any())
    }

    @Test
    fun `when getAppointmentSolicitationById with valid id then return appointment solicitation`() {
        // Arrange
        `when`(appointmentSolicitationRepository.findById("123")).thenReturn(Optional.of(appointmentSolicitation))

        // Act
        val result = appointmentSolicitationService.getAppointmentSolicitationById("123")

        // Assert
        assertEquals("123", result.id)
        assertEquals("John Doe", result.patientName)
        assertEquals(35, result.patientAge)
        assertEquals("Orthopedics", result.specialty)

        verify(appointmentSolicitationRepository, times(1)).findById("123")
    }

    @Test
    fun `when getAppointmentSolicitationById with invalid id then throw ResourceNotFoundException`() {
        // Arrange
        `when`(appointmentSolicitationRepository.findById("999")).thenReturn(Optional.empty())

        // Act & Assert
        val exception = assertThrows(ResourceNotFoundException::class.java) {
            appointmentSolicitationService.getAppointmentSolicitationById("999")
        }

        assertEquals("Appointment solicitation not found with id: 999", exception.message)
        verify(appointmentSolicitationRepository, times(1)).findById("999")
    }
} 