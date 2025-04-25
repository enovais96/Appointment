package com.sears.appointment.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.sears.appointment.dto.AppointmentSuggestionConfirmationDto
import com.sears.appointment.model.AppointmentSolicitation
import com.sears.appointment.model.AppointmentStatus
import com.sears.appointment.repositories.AppointmentSolicitationRepository
import com.sears.appointment.services.interfaces.AppointmentSolicitationService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AppointmentSolicitationControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var appointmentSolicitationRepository: AppointmentSolicitationRepository

    @Autowired
    private lateinit var appointmentSolicitationService: AppointmentSolicitationService

    private lateinit var objectMapper: ObjectMapper
    
    @BeforeEach
    fun setUp() {
        objectMapper = ObjectMapper().registerModule(JavaTimeModule())
        appointmentSolicitationRepository.deleteAll()
        
        // Create test data
        val solicitations = listOf(
            createTestSolicitation("id1", AppointmentStatus.PENDING),
            createTestSolicitation("id2", AppointmentStatus.SUGGESTED),
            createTestSolicitation("id3", AppointmentStatus.SUGGESTED),
            createTestSolicitation("id4", AppointmentStatus.SUGGESTED),
            createTestSolicitation("id5", AppointmentStatus.CONFIRMED),
            createTestSolicitation("id6", AppointmentStatus.REJECTED)
        )
        
        appointmentSolicitationRepository.saveAll(solicitations)
    }
    
    @AfterEach
    fun tearDown() {
        appointmentSolicitationRepository.deleteAll()
    }
    
    @Test
    @WithMockUser
    fun `getSuggestedAppointments should return paginated list of suggested appointments`() {
        // Act & Assert
        mockMvc.perform(
            get("/api/appointments/solicitations/suggested")
                .param("page", "0")
                .param("size", "2")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Suggested appointments retrieved successfully"))
            .andExpect(jsonPath("$.data.content.length()").value(2))
            .andExpect(jsonPath("$.data.totalElements").value(3))
            .andExpect(jsonPath("$.data.totalPages").value(2))
            .andExpect(jsonPath("$.data.content[0].status").value("SUGGESTED"))
            .andExpect(jsonPath("$.data.content[1].status").value("SUGGESTED"))
    }
    
    @Test
    @WithMockUser
    fun `getSuggestedAppointments should return second page when requested`() {
        // Act & Assert
        mockMvc.perform(
            get("/api/appointments/solicitations/suggested")
                .param("page", "1")
                .param("size", "2")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content.length()").value(1))
            .andExpect(jsonPath("$.data.totalElements").value(3))
            .andExpect(jsonPath("$.data.totalPages").value(2))
            .andExpect(jsonPath("$.data.number").value(1))
            .andExpect(jsonPath("$.data.content[0].status").value("SUGGESTED"))
    }
    
    @Test
    @WithMockUser
    fun `confirmSuggestedAppointment should confirm appointment when accept is true`() {
        // Arrange
        val id = "id2"
        val acceptDto = AppointmentSuggestionConfirmationDto(accept = true)
        
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
        
        // Verify the appointment was updated in the database
        val updatedSolicitation = appointmentSolicitationRepository.findById(id).orElseThrow()
        assert(updatedSolicitation.status == AppointmentStatus.CONFIRMED)
    }
    
    @Test
    @WithMockUser
    fun `confirmSuggestedAppointment should reject appointment when accept is false`() {
        // Arrange
        val id = "id3"
        val acceptDto = AppointmentSuggestionConfirmationDto(accept = false)
        
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
        
        // Verify the appointment was updated in the database
        val updatedSolicitation = appointmentSolicitationRepository.findById(id).orElseThrow()
        assert(updatedSolicitation.status == AppointmentStatus.REJECTED)
    }
    
    @Test
    @WithMockUser
    fun `confirmSuggestedAppointment should return 400 when appointment not in SUGGESTED status`() {
        // Arrange
        val id = "id1" // PENDING status
        val acceptDto = AppointmentSuggestionConfirmationDto(accept = true)
        
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
    }
    
    @Test
    @WithMockUser
    fun `confirmSuggestedAppointment should return 404 when appointment not found`() {
        // Arrange
        val id = "non-existent-id"
        val acceptDto = AppointmentSuggestionConfirmationDto(accept = true)
        
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
    }
    
    private fun createTestSolicitation(id: String, status: AppointmentStatus): AppointmentSolicitation {
        return AppointmentSolicitation(
            id = id,
            patientName = "Test Patient",
            patientAge = 30,
            patientPhone = "123-456-7890",
            patientEmail = "test@example.com",
            specialty = "Orthopedics",
            requestedDate = LocalDate.now(),
            requestedTime = "09:00",
            status = status,
            doctorId = if (status != AppointmentStatus.PENDING) "doctor-id" else null,
            suggestedDate = if (status == AppointmentStatus.SUGGESTED) LocalDate.now().plusDays(1) else null,
            suggestedTime = if (status == AppointmentStatus.SUGGESTED) "14:00" else null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }
} 