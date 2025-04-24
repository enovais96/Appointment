package com.sears.appointment.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.sears.appointment.dto.AppointmentSolicitationRequestDto
import com.sears.appointment.dto.AppointmentSolicitationResponseDto
import com.sears.appointment.global.exceptions.ResourceNotFoundException
import com.sears.appointment.model.AppointmentStatus
import com.sears.appointment.services.interfaces.AppointmentSolicitationService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate
import java.time.LocalTime
import org.mockito.Mockito.`when`

@WebMvcTest(AppointmentSolicitationController::class)
class AppointmentSolicitationControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var appointmentSolicitationService: AppointmentSolicitationService

    private lateinit var objectMapper: ObjectMapper
    private lateinit var requestDto: AppointmentSolicitationRequestDto
    private lateinit var responseDto: AppointmentSolicitationResponseDto

    @BeforeEach
    fun setUp() {
        objectMapper = ObjectMapper()
        objectMapper.registerModule(JavaTimeModule())

        requestDto = AppointmentSolicitationRequestDto(
            patientName = "John Doe",
            patientAge = 35,
            patientPhone = "555-123-4567",
            patientEmail = "john.doe@example.com",
            specialty = "Orthopedics",
            requestedDate = LocalDate.of(2025, 4, 23),
            requestedTime = LocalTime.of(9, 0)
        )

        responseDto = AppointmentSolicitationResponseDto(
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
    }

    @Test
    @WithMockUser
    fun `when createAppointmentSolicitation with valid data then return 201 and solicitation data`() {
        // Arrange
        `when`(appointmentSolicitationService.createAppointmentSolicitation(requestDto))
            .thenReturn(responseDto)

        // Act & Assert
        mockMvc.perform(
            post("/api/appointments/solicitations")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Appointment solicitation created and sent for processing"))
            .andExpect(jsonPath("$.data.id").value("123"))
            .andExpect(jsonPath("$.data.patientName").value("John Doe"))
            .andExpect(jsonPath("$.data.specialty").value("Orthopedics"))
            .andExpect(jsonPath("$.data.status").value("PENDING"))
    }

    @Test
    @WithMockUser
    fun `when getAppointmentSolicitationById with valid id then return 200 and solicitation data`() {
        // Arrange
        `when`(appointmentSolicitationService.getAppointmentSolicitationById("123"))
            .thenReturn(responseDto)

        // Act & Assert
        mockMvc.perform(
            get("/api/appointments/solicitations/123")
                .with(csrf())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Appointment solicitation retrieved successfully"))
            .andExpect(jsonPath("$.data.id").value("123"))
            .andExpect(jsonPath("$.data.patientName").value("John Doe"))
    }

    @Test
    @WithMockUser
    fun `when getAppointmentSolicitationById with invalid id then return 404`() {
        // Arrange
        `when`(appointmentSolicitationService.getAppointmentSolicitationById("999"))
            .thenThrow(ResourceNotFoundException("Appointment solicitation not found with id: 999"))

        // Act & Assert
        mockMvc.perform(
            get("/api/appointments/solicitations/999")
                .with(csrf())
        )
            .andExpect(status().isNotFound)
    }
} 