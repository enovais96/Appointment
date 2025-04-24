package com.sears.appointment.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.sears.appointment.dto.AvailabilitySlotDto
import com.sears.appointment.dto.DoctorRequestDto
import com.sears.appointment.dto.DoctorResponseDto
import com.sears.appointment.dto.DoctorUpdateDto
import com.sears.appointment.global.exceptions.ResourceNotFoundException
import com.sears.appointment.model.DayOfWeek
import com.sears.appointment.services.interfaces.DoctorService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
class DoctorControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var doctorService: DoctorService

    private val testAvailabilitySlot = AvailabilitySlotDto(
        dayOfWeek = DayOfWeek.MONDAY,
        startTime = "08:00",
        endTime = "11:00"
    )

    private val testDoctorResponse = DoctorResponseDto(
        id = "123",
        name = "Dr. Smith",
        specialty = "Orthopedics",
        availabilitySchedule = listOf(testAvailabilitySlot)
    )

    @Test
    @WithMockUser
    fun `when POST to api-doctors then return 201 Created`() {
        // Arrange
        val doctorRequest = DoctorRequestDto(
            name = "Dr. Smith",
            specialty = "Orthopedics",
            availabilitySchedule = listOf(testAvailabilitySlot)
        )

        `when`(doctorService.createDoctor(any(DoctorRequestDto::class.java)))
            .thenReturn(testDoctorResponse)

        // Act & Assert
        mockMvc.perform(post("/api/doctors")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(doctorRequest)))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value("123"))
            .andExpect(jsonPath("$.data.name").value("Dr. Smith"))
            .andExpect(jsonPath("$.data.specialty").value("Orthopedics"))
    }

    @Test
    @WithMockUser
    fun `when GET to api-doctors then return 200 OK and list of doctors`() {
        // Arrange
        `when`(doctorService.getAllDoctors()).thenReturn(listOf(testDoctorResponse))

        // Act & Assert
        mockMvc.perform(get("/api/doctors"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].id").value("123"))
            .andExpect(jsonPath("$.data[0].name").value("Dr. Smith"))
    }

    @Test
    @WithMockUser
    fun `when GET to api-doctors-id with valid id then return 200 OK and doctor`() {
        // Arrange
        `when`(doctorService.getDoctorById("123")).thenReturn(testDoctorResponse)

        // Act & Assert
        mockMvc.perform(get("/api/doctors/123"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value("123"))
            .andExpect(jsonPath("$.data.name").value("Dr. Smith"))
    }

    @Test
    @WithMockUser
    fun `when GET to api-doctors-id with invalid id then return 404 Not Found`() {
        // Arrange
        `when`(doctorService.getDoctorById("456"))
            .thenThrow(ResourceNotFoundException("Doctor not found with id: 456"))

        // Act & Assert
        mockMvc.perform(get("/api/doctors/456"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Doctor not found with id: 456"))
    }

    @Test
    @WithMockUser
    fun `when PUT to api-doctors-id with valid id then return 200 OK and updated doctor`() {
        // Arrange
        val doctorUpdateDto = DoctorUpdateDto(
            name = "Dr. John Smith",
            specialty = "Neurology"
        )

        val updatedDoctorResponse = DoctorResponseDto(
            id = "123",
            name = "Dr. John Smith",
            specialty = "Neurology",
            availabilitySchedule = listOf(testAvailabilitySlot)
        )

        `when`(doctorService.updateDoctor(eq("123"), any(DoctorUpdateDto::class.java)))
            .thenReturn(updatedDoctorResponse)

        // Act & Assert
        mockMvc.perform(put("/api/doctors/123")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(doctorUpdateDto)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value("123"))
            .andExpect(jsonPath("$.data.name").value("Dr. John Smith"))
            .andExpect(jsonPath("$.data.specialty").value("Neurology"))
    }

    @Test
    @WithMockUser
    fun `when DELETE to api-doctors-id with valid id then return 200 OK`() {
        // Arrange
        doNothing().`when`(doctorService).deleteDoctor("123")

        // Act & Assert
        mockMvc.perform(delete("/api/doctors/123"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Doctor deleted successfully"))
    }

    @Test
    @WithMockUser
    fun `when DELETE to api-doctors-id with invalid id then return 404 Not Found`() {
        // Arrange
        doThrow(ResourceNotFoundException("Doctor not found with id: 456"))
            .`when`(doctorService).deleteDoctor("456")

        // Act & Assert
        mockMvc.perform(delete("/api/doctors/456"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Doctor not found with id: 456"))
    }

    @Test
    @WithMockUser
    fun `when GET to api-doctors-specialty-name then return 200 OK and list of doctors`() {
        // Arrange
        `when`(doctorService.getDoctorsBySpecialty("Orthopedics"))
            .thenReturn(listOf(testDoctorResponse))

        // Act & Assert
        mockMvc.perform(get("/api/doctors/specialty/Orthopedics"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].id").value("123"))
            .andExpect(jsonPath("$.data[0].specialty").value("Orthopedics"))
    }
} 