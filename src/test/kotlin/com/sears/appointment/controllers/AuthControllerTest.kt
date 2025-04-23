package com.sears.appointment.controllers

import com.ninjasquad.springmockk.MockkBean
import com.sears.appointment.dto.TokenResponseDto
import com.sears.appointment.services.interfaces.AuthService
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(AuthController::class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var authService: AuthService

    @Test
    @WithMockUser
    fun `should login user successfully`() {
        // Arrange
        val email = "test@example.com"
        val password = "Test@123"
        
        val jsonContent = """
            {
                "email": "$email",
                "password": "$password"
            }
        """.trimIndent()
        
        val tokenResponseDto = TokenResponseDto(
            accessToken = "mock-access-token",
            refreshToken = "mock-refresh-token"
        )
        
        every { 
            authService.loginUser(any()) 
        } returns tokenResponseDto
        
        // Act & Assert
        mockMvc.perform(
            post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Authentication successful"))
            .andExpect(jsonPath("$.data.accessToken").value("mock-access-token"))
            .andExpect(jsonPath("$.data.refreshToken").value("mock-refresh-token"))
    }
    
    @Test
    @WithMockUser
    fun `should return 401 for invalid credentials`() {
        // Arrange
        val email = "test@example.com"
        val password = "WrongPassword"
        
        val jsonContent = """
            {
                "email": "$email",
                "password": "$password"
            }
        """.trimIndent()
        
        every { 
            authService.loginUser(any()) 
        } throws BadCredentialsException("Invalid email or password")
        
        // Act & Assert
        mockMvc.perform(
            post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent)
        )
            .andExpect(status().isUnauthorized)
    }
    
    @Test
    @WithMockUser
    fun `should refresh token successfully`() {
        // Arrange
        val refreshToken = "valid-refresh-token"
        
        val jsonContent = """
            {
                "refreshToken": "$refreshToken"
            }
        """.trimIndent()
        
        val tokenResponseDto = TokenResponseDto(
            accessToken = "new-access-token",
            refreshToken = "new-refresh-token"
        )
        
        every { 
            authService.refreshToken(refreshToken) 
        } returns tokenResponseDto
        
        // Act & Assert
        mockMvc.perform(
            post("/api/auth/refresh-token")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Token refreshed successfully"))
            .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
            .andExpect(jsonPath("$.data.refreshToken").value("new-refresh-token"))
    }
    
    @Test
    @WithMockUser
    fun `should return 400 for invalid refresh token`() {
        // Arrange
        val refreshToken = "invalid-refresh-token"
        
        val jsonContent = """
            {
                "refreshToken": "$refreshToken"
            }
        """.trimIndent()
        
        every { 
            authService.refreshToken(refreshToken) 
        } throws IllegalArgumentException("Invalid refresh token")
        
        // Act & Assert
        mockMvc.perform(
            post("/api/auth/refresh-token")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Invalid refresh token"))
    }
} 