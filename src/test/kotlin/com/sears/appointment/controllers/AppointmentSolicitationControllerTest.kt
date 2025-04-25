package com.sears.appointment.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.ninjasquad.springmockk.MockkBean
import com.sears.appointment.dto.AppointmentSolicitationRequestDto
import com.sears.appointment.dto.AppointmentSolicitationResponseDto
import com.sears.appointment.dto.AppointmentSuggestionConfirmationDto
import com.sears.appointment.global.exceptions.BadRequestException
import com.sears.appointment.global.exceptions.ResourceNotFoundException
import com.sears.appointment.model.AppointmentStatus
import com.sears.appointment.services.interfaces.AppointmentSolicitationService
import com.sears.appointment.utils.JwtUtils
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
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

@SpringBootTest
@AutoConfigureMockMvc
class AppointmentSolicitationControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var appointmentSolicitationService: AppointmentSolicitationService

    @MockkBean
    private lateinit var jwtUtils: JwtUtils

    private lateinit var objectMapper: ObjectMapper
    private lateinit var requestDto: AppointmentSolicitationRequestDto
    private lateinit var mockAppointmentSolicitationResponse: AppointmentSolicitationResponseDto

    @BeforeEach
    fun setUp() {
        objectMapper = ObjectMapper().registerModule(JavaTimeModule())

        requestDto = AppointmentSolicitationRequestDto(
            patientName = "John Doe",
            patientAge = 35,
            patientPhone = "555-123-4567",
            patientEmail = "john.doe@example.com",
            specialty = "Orthopedics",
            requestedDate = LocalDate.of(2025, 4, 23),
            requestedTime = LocalTime.of(9, 0)
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
    @WithMockUser
    fun `when createAppointmentSolicitation with valid data then return 201 and solicitation data`() {
        // Arrange
        every { 
            appointmentSolicitationService.createAppointmentSolicitation(any()) 
        } returns mockAppointmentSolicitationResponse

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
            .andExpect(jsonPath("$.data.id").value("test-id"))
            .andExpect(jsonPath("$.data.patientName").value("John Doe"))
            .andExpect(jsonPath("$.data.specialty").value("Orthopedics"))
            .andExpect(jsonPath("$.data.status").value("SUGGESTED"))

        verify(exactly = 1) { 
            appointmentSolicitationService.createAppointmentSolicitation(any()) 
        }
    }

    @Test
    @WithMockUser
    fun `when getAppointmentSolicitationById with valid id then return 200 and solicitation data`() {
        // Arrange
        every { 
            appointmentSolicitationService.getAppointmentSolicitationById(any()) 
        } returns mockAppointmentSolicitationResponse

        // Act & Assert
        mockMvc.perform(
            get("/api/appointments/solicitations/test-id")
                .with(csrf())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Appointment solicitation retrieved successfully"))
            .andExpect(jsonPath("$.data.id").value("test-id"))
            .andExpect(jsonPath("$.data.patientName").value("John Doe"))

        verify(exactly = 1) { 
            appointmentSolicitationService.getAppointmentSolicitationById(any()) 
        }
    }

    @Test
    @WithMockUser
    fun `when getAppointmentSolicitationById with invalid id then return 404`() {
        // Arrange
        every { 
            appointmentSolicitationService.getAppointmentSolicitationById(any()) 
        } throws ResourceNotFoundException("Appointment solicitation not found with id: 999")

        // Act & Assert
        mockMvc.perform(
            get("/api/appointments/solicitations/999")
                .with(csrf())
        )
            .andExpect(status().isNotFound)

        verify(exactly = 1) { 
            appointmentSolicitationService.getAppointmentSolicitationById(any()) 
        }
    }

    @Test
    @WithMockUser
    fun `getSuggestedAppointments should return paginated list of suggested appointments`() {
        // Arrange
        val page = 0
        val size = 10
        val pageable = PageRequest.of(page, size)
        val appointmentsList = listOf(mockAppointmentSolicitationResponse)
        val appointmentsPage = PageImpl(appointmentsList, pageable, appointmentsList.size.toLong())

        every { 
            appointmentSolicitationService.getSuggestedAppointments(any()) 
        } returns appointmentsPage

        // Act & Assert
        mockMvc.perform(
            get("/api/appointments/solicitations/suggested")
                .param("page", page.toString())
                .param("size", size.toString())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Suggested appointments retrieved successfully"))
            .andExpect(jsonPath("$.data.content[0].id").value("test-id"))
            .andExpect(jsonPath("$.data.content[0].status").value("SUGGESTED"))
            .andExpect(jsonPath("$.data.totalElements").value(1))

        verify(exactly = 1) { 
            appointmentSolicitationService.getSuggestedAppointments(any()) 
        }
    }

    @Test
    @WithMockUser
    fun `getSuggestedAppointments should return empty page when no suggested appointments exist`() {
        // Arrange
        val page = 0
        val size = 10
        val pageable = PageRequest.of(page, size)
        val emptyPage = PageImpl(emptyList<AppointmentSolicitationResponseDto>(), pageable, 0)

        every { 
            appointmentSolicitationService.getSuggestedAppointments(any()) 
        } returns emptyPage

        // Act & Assert
        mockMvc.perform(
            get("/api/appointments/solicitations/suggested")
                .param("page", page.toString())
                .param("size", size.toString())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Suggested appointments retrieved successfully"))
            .andExpect(jsonPath("$.data.content").isEmpty())
            .andExpect(jsonPath("$.data.totalElements").value(0))

        verify(exactly = 1) { 
            appointmentSolicitationService.getSuggestedAppointments(any()) 
        }
    }

    @Test
    @WithMockUser
    fun `confirmSuggestedAppointment should confirm appointment when accept is true`() {
        // Arrange
        val id = "test-id"
        val acceptDto = AppointmentSuggestionConfirmationDto(accept = true)
        
        val confirmedAppointment = mockAppointmentSolicitationResponse.copy(
            status = AppointmentStatus.CONFIRMED
        )

        every { 
            appointmentSolicitationService.confirmSuggestedAppointment(id, true) 
        } returns confirmedAppointment

        // Act & Assert
        mockMvc.perform(
            post("/api/appointments/solicitations/$id/confirm")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(acceptDto))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Appointment confirmed successfully"))
            .andExpect(jsonPath("$.data.id").value(id))
            .andExpect(jsonPath("$.data.status").value("CONFIRMED"))

        verify(exactly = 1) { 
            appointmentSolicitationService.confirmSuggestedAppointment(id, true) 
        }
    }

    @Test
    @WithMockUser
    fun `confirmSuggestedAppointment should reject appointment when accept is false`() {
        // Arrange
        val id = "test-id"
        val acceptDto = AppointmentSuggestionConfirmationDto(accept = false)
        
        val rejectedAppointment = mockAppointmentSolicitationResponse.copy(
            status = AppointmentStatus.REJECTED
        )

        every { 
            appointmentSolicitationService.confirmSuggestedAppointment(id, false) 
        } returns rejectedAppointment

        // Act & Assert
        mockMvc.perform(
            post("/api/appointments/solicitations/$id/confirm")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(acceptDto))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Appointment rejected successfully"))
            .andExpect(jsonPath("$.data.id").value(id))
            .andExpect(jsonPath("$.data.status").value("REJECTED"))

        verify(exactly = 1) { 
            appointmentSolicitationService.confirmSuggestedAppointment(id, false) 
        }
    }

    @Test
    @WithMockUser
    fun `confirmSuggestedAppointment should return 400 when appointment not in SUGGESTED status`() {
        // Arrange
        val id = "test-id"
        val acceptDto = AppointmentSuggestionConfirmationDto(accept = true)

        every { 
            appointmentSolicitationService.confirmSuggestedAppointment(id, true) 
        } throws BadRequestException("No suggested appointment found with id: $id")

        // Act & Assert
        mockMvc.perform(
            post("/api/appointments/solicitations/$id/confirm")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(acceptDto))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("No suggested appointment found with id: $id"))

        verify(exactly = 1) { 
            appointmentSolicitationService.confirmSuggestedAppointment(id, true) 
        }
    }

    @Test
    @WithMockUser
    fun `confirmSuggestedAppointment should return 404 when appointment not found`() {
        // Arrange
        val id = "non-existent-id"
        val acceptDto = AppointmentSuggestionConfirmationDto(accept = true)

        every { 
            appointmentSolicitationService.confirmSuggestedAppointment(id, true) 
        } throws ResourceNotFoundException("Appointment solicitation not found with id: $id")

        // Act & Assert
        mockMvc.perform(
            post("/api/appointments/solicitations/$id/confirm")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(acceptDto))
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Appointment solicitation not found with id: $id"))

        verify(exactly = 1) { 
            appointmentSolicitationService.confirmSuggestedAppointment(id, true) 
        }
    }

    @Test
    fun `confirmSuggestedAppointment should return 403 when user not authenticated`() {
        // Arrange
        val id = "test-id"
        val acceptDto = AppointmentSuggestionConfirmationDto(accept = true)

        // Act & Assert
        mockMvc.perform(
            post("/api/appointments/solicitations/$id/confirm")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(acceptDto))
        )
            .andExpect(status().isForbidden)

        verify(exactly = 0) { 
            appointmentSolicitationService.confirmSuggestedAppointment(any(), any()) 
        }
    }
} 