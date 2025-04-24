package com.sears.appointment.services

import com.sears.appointment.dto.AvailabilitySlotDto
import com.sears.appointment.dto.DoctorRequestDto
import com.sears.appointment.dto.DoctorUpdateDto
import com.sears.appointment.global.exceptions.ResourceNotFoundException
import com.sears.appointment.model.AvailabilitySlot
import com.sears.appointment.model.DayOfWeek
import com.sears.appointment.model.Doctor
import com.sears.appointment.repositories.DoctorRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*

@ExtendWith(MockitoExtension::class)
class DoctorServiceImplTest {

    @Mock
    private lateinit var doctorRepository: DoctorRepository

    @InjectMocks
    private lateinit var doctorService: DoctorServiceImpl

    private lateinit var testDoctor: Doctor
    private lateinit var testAvailabilitySlot: AvailabilitySlot
    private lateinit var testAvailabilitySlotDto: AvailabilitySlotDto

    @BeforeEach
    fun setUp() {
        testAvailabilitySlot = AvailabilitySlot(
            dayOfWeek = DayOfWeek.MONDAY,
            startTime = "08:00",
            endTime = "11:00"
        )

        testAvailabilitySlotDto = AvailabilitySlotDto(
            dayOfWeek = DayOfWeek.MONDAY,
            startTime = "08:00",
            endTime = "11:00"
        )

        testDoctor = Doctor(
            id = "123",
            name = "Dr. Smith",
            specialty = "Orthopedics",
            availabilitySchedule = listOf(testAvailabilitySlot)
        )
    }

    @Test
    fun `when createDoctor then return DoctorResponseDto`() {
        // Arrange
        val doctorRequestDto = DoctorRequestDto(
            name = "Dr. Smith",
            specialty = "Orthopedics",
            availabilitySchedule = listOf(testAvailabilitySlotDto)
        )

        `when`(doctorRepository.save(any(Doctor::class.java))).thenReturn(testDoctor)

        // Act
        val result = doctorService.createDoctor(doctorRequestDto)

        // Assert
        assertEquals("123", result.id)
        assertEquals("Dr. Smith", result.name)
        assertEquals("Orthopedics", result.specialty)
        assertEquals(1, result.availabilitySchedule.size)
        assertEquals(DayOfWeek.MONDAY, result.availabilitySchedule[0].dayOfWeek)
        verify(doctorRepository, times(1)).save(any(Doctor::class.java))
    }

    @Test
    fun `when getAllDoctors then return list of DoctorResponseDto`() {
        // Arrange
        `when`(doctorRepository.findAll()).thenReturn(listOf(testDoctor))

        // Act
        val result = doctorService.getAllDoctors()

        // Assert
        assertEquals(1, result.size)
        assertEquals("123", result[0].id)
        assertEquals("Dr. Smith", result[0].name)
        verify(doctorRepository, times(1)).findAll()
    }

    @Test
    fun `when getDoctorById with valid id then return DoctorResponseDto`() {
        // Arrange
        `when`(doctorRepository.findById("123")).thenReturn(Optional.of(testDoctor))

        // Act
        val result = doctorService.getDoctorById("123")

        // Assert
        assertEquals("123", result.id)
        assertEquals("Dr. Smith", result.name)
        verify(doctorRepository, times(1)).findById("123")
    }

    @Test
    fun `when getDoctorById with invalid id then throw ResourceNotFoundException`() {
        // Arrange
        `when`(doctorRepository.findById("456")).thenReturn(Optional.empty())

        // Act & Assert
        val exception = assertThrows(ResourceNotFoundException::class.java) {
            doctorService.getDoctorById("456")
        }
        assertEquals("Doctor not found with id: 456", exception.message)
        verify(doctorRepository, times(1)).findById("456")
    }

    @Test
    fun `when updateDoctor with valid id then return updated DoctorResponseDto`() {
        // Arrange
        val doctorUpdateDto = DoctorUpdateDto(
            name = "Dr. John Smith",
            specialty = "Neurology",
            availabilitySchedule = listOf(
                AvailabilitySlotDto(
                    dayOfWeek = DayOfWeek.TUESDAY,
                    startTime = "09:00",
                    endTime = "12:00"
                )
            )
        )

        val updatedDoctor = Doctor(
            id = "123",
            name = "Dr. John Smith",
            specialty = "Neurology",
            availabilitySchedule = listOf(
                AvailabilitySlot(
                    dayOfWeek = DayOfWeek.TUESDAY,
                    startTime = "09:00",
                    endTime = "12:00"
                )
            )
        )

        `when`(doctorRepository.findById("123")).thenReturn(Optional.of(testDoctor))
        `when`(doctorRepository.save(any(Doctor::class.java))).thenReturn(updatedDoctor)

        // Act
        val result = doctorService.updateDoctor("123", doctorUpdateDto)

        // Assert
        assertEquals("123", result.id)
        assertEquals("Dr. John Smith", result.name)
        assertEquals("Neurology", result.specialty)
        assertEquals(DayOfWeek.TUESDAY, result.availabilitySchedule[0].dayOfWeek)
        verify(doctorRepository, times(1)).findById("123")
        verify(doctorRepository, times(1)).save(any(Doctor::class.java))
    }

    @Test
    fun `when updateDoctor with invalid id then throw ResourceNotFoundException`() {
        // Arrange
        val doctorUpdateDto = DoctorUpdateDto(name = "Dr. John Smith")
        `when`(doctorRepository.findById("456")).thenReturn(Optional.empty())

        // Act & Assert
        val exception = assertThrows(ResourceNotFoundException::class.java) {
            doctorService.updateDoctor("456", doctorUpdateDto)
        }
        assertEquals("Doctor not found with id: 456", exception.message)
        verify(doctorRepository, times(1)).findById("456")
        verify(doctorRepository, never()).save(any(Doctor::class.java))
    }

    @Test
    fun `when deleteDoctor with valid id then delete doctor`() {
        // Arrange
        `when`(doctorRepository.existsById("123")).thenReturn(true)
        doNothing().`when`(doctorRepository).deleteById("123")

        // Act
        doctorService.deleteDoctor("123")

        // Assert
        verify(doctorRepository, times(1)).existsById("123")
        verify(doctorRepository, times(1)).deleteById("123")
    }

    @Test
    fun `when deleteDoctor with invalid id then throw ResourceNotFoundException`() {
        // Arrange
        `when`(doctorRepository.existsById("456")).thenReturn(false)

        // Act & Assert
        val exception = assertThrows(ResourceNotFoundException::class.java) {
            doctorService.deleteDoctor("456")
        }
        assertEquals("Doctor not found with id: 456", exception.message)
        verify(doctorRepository, times(1)).existsById("456")
        verify(doctorRepository, never()).deleteById(anyString())
    }

    @Test
    fun `when getDoctorsBySpecialty then return list of DoctorResponseDto`() {
        // Arrange
        `when`(doctorRepository.findBySpecialty("Orthopedics")).thenReturn(listOf(testDoctor))

        // Act
        val result = doctorService.getDoctorsBySpecialty("Orthopedics")

        // Assert
        assertEquals(1, result.size)
        assertEquals("123", result[0].id)
        assertEquals("Dr. Smith", result[0].name)
        assertEquals("Orthopedics", result[0].specialty)
        verify(doctorRepository, times(1)).findBySpecialty("Orthopedics")
    }
} 