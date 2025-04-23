package com.sears.appointment.controllers

import com.ninjasquad.springmockk.MockkBean
import com.sears.appointment.dto.UserRegistrationDto
import com.sears.appointment.dto.UserResponseDto
import com.sears.appointment.services.interfaces.UserService
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(UserController::class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var userService: UserService

    @Test
    @WithMockUser
    fun `should register user successfully`() {
        // Arrange
        val email = "test@example.com"
        val password = "Test@123"
        val userId = "1234"
        
        val jsonContent = """
            {
                "email": "$email",
                "password": "$password"
            }
        """.trimIndent()
        
        val userResponseDto = UserResponseDto(
            id = userId,
            email = email
        )
        
        every { 
            userService.registerUser(any()) 
        } returns userResponseDto
        
        // Act & Assert
        mockMvc.perform(
            post("/api/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent)
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("User registered successfully"))
            .andExpect(jsonPath("$.data.id").value(userId))
            .andExpect(jsonPath("$.data.email").value(email))
    }
    
    @Test
    @WithMockUser
    fun `should return 400 when email already exists`() {
        // Arrange
        val email = "existing@example.com"
        val password = "Test@123"
        
        val jsonContent = """
            {
                "email": "$email",
                "password": "$password"
            }
        """.trimIndent()
        
        every { 
            userService.registerUser(any()) 
        } throws IllegalArgumentException("User with email $email already exists")
        
        // Act & Assert
        mockMvc.perform(
            post("/api/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("User with email $email already exists"))
    }
    
    @Test
    @WithMockUser
    fun `should validate registration input`() {
        // Arrange
        val email = "invalid-email"
        val password = "weak"
        
        val jsonContent = """
            {
                "email": "$email",
                "password": "$password"
            }
        """.trimIndent()
        
        // Act & Assert
        mockMvc.perform(
            post("/api/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent)
        )
            .andExpect(status().isBadRequest)
    }
} 