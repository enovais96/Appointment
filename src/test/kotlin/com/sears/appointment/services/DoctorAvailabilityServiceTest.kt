package com.sears.appointment.services

import com.sears.appointment.model.AvailabilitySlot
import com.sears.appointment.model.DayOfWeek
import com.sears.appointment.model.Doctor
import com.sears.appointment.model.DoctorAvailability
import com.sears.appointment.model.TimeSlot
import com.sears.appointment.repositories.DoctorAvailabilityRepository
import com.sears.appointment.repositories.DoctorRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class DoctorAvailabilityServiceTest {

    @Mock
    private lateinit var doctorAvailabilityRepository: DoctorAvailabilityRepository

    @Mock
    private lateinit var doctorRepository: DoctorRepository

    @InjectMocks
    private lateinit var doctorAvailabilityService: DoctorAvailabilityServiceImpl

    @Test
    fun `generateDoctorAvailability should create timeslots based on doctor schedule`() {
        // Arrange
        val doctorId = "doctor-123"
        val fromDate = LocalDate.of(2025, 4, 23) // Wednesday
        val toDate = LocalDate.of(2025, 4, 25) // Friday
        
        val doctor = Doctor(
            id = doctorId,
            name = "Dr. Smith",
            specialty = "Cardiology",
            availabilitySchedule = listOf(
                AvailabilitySlot(
                    dayOfWeek = DayOfWeek.WEDNESDAY,
                    startTime = "09:00",
                    endTime = "12:00"
                ),
                AvailabilitySlot(
                    dayOfWeek = DayOfWeek.FRIDAY,
                    startTime = "14:00",
                    endTime = "17:00"
                )
            )
        )
        
        // Existing availabilities that should be deleted
        val existingAvailabilities = listOf(
            DoctorAvailability(
                id = "avail-1",
                doctorId = doctorId,
                date = fromDate,
                timeSlots = emptyList()
            )
        )
        
        // Expected created availabilities
        val wednesdayAvailability = DoctorAvailability(
            id = "new-avail-1",
            doctorId = doctorId,
            date = fromDate,
            timeSlots = listOf(
                TimeSlot("09:00", "09:30", true, null),
                TimeSlot("09:30", "10:00", true, null),
                TimeSlot("10:00", "10:30", true, null),
                TimeSlot("10:30", "11:00", true, null),
                TimeSlot("11:00", "11:30", true, null),
                TimeSlot("11:30", "12:00", true, null)
            )
        )
        
        val fridayAvailability = DoctorAvailability(
            id = "new-avail-2",
            doctorId = doctorId,
            date = toDate,
            timeSlots = listOf(
                TimeSlot("14:00", "14:30", true, null),
                TimeSlot("14:30", "15:00", true, null),
                TimeSlot("15:00", "15:30", true, null),
                TimeSlot("15:30", "16:00", true, null),
                TimeSlot("16:00", "16:30", true, null),
                TimeSlot("16:30", "17:00", true, null)
            )
        )
        
        // Mock repository behavior
        whenever(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor))
        whenever(doctorAvailabilityRepository.findByDoctorId(doctorId)).thenReturn(existingAvailabilities)
        whenever(doctorAvailabilityRepository.save(any<DoctorAvailability>()))
            .thenAnswer { it.arguments[0] as DoctorAvailability }
        
        // Act
        val result = doctorAvailabilityService.generateDoctorAvailability(doctorId, fromDate, toDate)
        
        // Assert
        verify(doctorRepository, times(1)).findById(doctorId)
        verify(doctorAvailabilityRepository, times(1)).findByDoctorId(doctorId)
        verify(doctorAvailabilityRepository, times(1)).delete(existingAvailabilities[0])
        
        // Verify saves (we expect 2 availabilities, for Wednesday and Friday)
        verify(doctorAvailabilityRepository, times(2)).save(any<DoctorAvailability>())
    }
    
    @Test
    fun `bookTimeSlot should mark time slot as unavailable`() {
        // Arrange
        val doctorId = "doctor-123"
        val date = LocalDate.of(2025, 4, 23)
        val startTime = LocalTime.of(9, 0)
        val appointmentId = "appointment-123"
        
        val timeSlots = listOf(
            TimeSlot("09:00", "09:30", true, null),
            TimeSlot("09:30", "10:00", true, null)
        )
        
        val availability = DoctorAvailability(
            id = "avail-1",
            doctorId = doctorId,
            date = date,
            timeSlots = timeSlots
        )
        
        val expectedUpdatedTimeSlots = listOf(
            TimeSlot("09:00", "09:30", false, appointmentId),
            TimeSlot("09:30", "10:00", true, null)
        )
        
        val expectedUpdatedAvailability = availability.copy(
            timeSlots = expectedUpdatedTimeSlots,
            updatedAt = System.currentTimeMillis()
        )
        
        // Mock repository behavior
        whenever(doctorAvailabilityRepository.findByDoctorIdAndDate(doctorId, date))
            .thenReturn(Optional.of(availability))
        whenever(doctorAvailabilityRepository.save(any<DoctorAvailability>()))
            .thenAnswer { it.arguments[0] as DoctorAvailability }
        
        // Act
        val result = doctorAvailabilityService.bookTimeSlot(doctorId, date, startTime, appointmentId)
        
        // Assert
        verify(doctorAvailabilityRepository, times(1)).findByDoctorIdAndDate(doctorId, date)
        verify(doctorAvailabilityRepository, times(1)).save(any<DoctorAvailability>())
        assert(result)
    }
    
    @Test
    fun `isTimeSlotAvailable should check availability correctly`() {
        // Arrange
        val doctorId = "doctor-123"
        val date = LocalDate.of(2025, 4, 23)
        val availableTime = LocalTime.of(9, 0)
        val unavailableTime = LocalTime.of(10, 0)
        
        val timeSlots = listOf(
            TimeSlot("09:00", "09:30", true, null),
            TimeSlot("10:00", "10:30", false, "some-appointment-id")
        )
        
        val availability = DoctorAvailability(
            id = "avail-1",
            doctorId = doctorId,
            date = date,
            timeSlots = timeSlots
        )
        
        // Mock repository behavior
        whenever(doctorAvailabilityRepository.findByDoctorIdAndDate(doctorId, date))
            .thenReturn(Optional.of(availability))
        
        // Act
        val availableResult = doctorAvailabilityService.isTimeSlotAvailable(doctorId, date, availableTime)
        val unavailableResult = doctorAvailabilityService.isTimeSlotAvailable(doctorId, date, unavailableTime)
        
        // Assert
        verify(doctorAvailabilityRepository, times(2)).findByDoctorIdAndDate(doctorId, date)
        assert(availableResult)
        assert(!unavailableResult)
    }
} 