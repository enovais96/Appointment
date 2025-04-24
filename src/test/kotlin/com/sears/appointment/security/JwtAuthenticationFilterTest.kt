package com.sears.appointment.security

import com.sears.appointment.model.User
import com.sears.appointment.repositories.UserRepository
import com.sears.appointment.utils.JwtUtils
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.core.context.SecurityContextHolder
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class JwtAuthenticationFilterTest {

    private lateinit var jwtUtils: JwtUtils
    private lateinit var userRepository: UserRepository
    private lateinit var filter: JwtAuthenticationFilter
    private lateinit var request: HttpServletRequest
    private lateinit var response: HttpServletResponse
    private lateinit var filterChain: FilterChain

    @BeforeEach
    fun setUp() {
        jwtUtils = mockk()
        userRepository = mockk()
        filter = JwtAuthenticationFilter(jwtUtils, userRepository)
        request = mockk(relaxed = true)
        response = mockk(relaxed = true)
        filterChain = mockk(relaxed = true)
        
        // Clear security context before each test
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `when no authorization header, should continue filter chain`() {
        // Arrange
        every { request.getHeader("Authorization") } returns null
        
        // Act
        filter.doFilterInternal(request, response, filterChain)
        
        // Assert
        verify { filterChain.doFilter(request, response) }
    }

    @Test
    fun `when authorization header does not start with Bearer, should continue filter chain`() {
        // Arrange
        every { request.getHeader("Authorization") } returns "Token abc123"
        
        // Act
        filter.doFilterInternal(request, response, filterChain)
        
        // Assert
        verify { filterChain.doFilter(request, response) }
    }

    @Test
    fun `when token is expired, should continue filter chain`() {
        // Arrange
        every { request.getHeader("Authorization") } returns "Bearer abc123"
        every { jwtUtils.isTokenExpired("abc123") } returns true
        
        // Act
        filter.doFilterInternal(request, response, filterChain)
        
        // Assert
        verify { filterChain.doFilter(request, response) }
    }
    
    @Test
    fun `when token is not an access token, should continue filter chain`() {
        // Arrange
        val token = "abc123"
        
        every { request.getHeader("Authorization") } returns "Bearer $token"
        every { jwtUtils.isTokenExpired(token) } returns false
        every { jwtUtils.isAccessToken(token) } returns false
        
        // Act
        filter.doFilterInternal(request, response, filterChain)
        
        // Assert
        verify { filterChain.doFilter(request, response) }
    }

    @Test
    fun `when user does not exist, should continue filter chain`() {
        // Arrange
        val token = "abc123"
        val userId = "user123"
        
        every { request.getHeader("Authorization") } returns "Bearer $token"
        every { jwtUtils.isTokenExpired(token) } returns false
        every { jwtUtils.isAccessToken(token) } returns true
        every { jwtUtils.getUserIdFromToken(token) } returns userId
        every { userRepository.findById(userId) } returns Optional.empty()
        
        // Act
        filter.doFilterInternal(request, response, filterChain)
        
        // Assert
        verify { filterChain.doFilter(request, response) }
    }

    @Test
    fun `when valid token and user exists, should set authentication in security context`() {
        // Arrange
        val token = "abc123"
        val userId = "user123"
        val user = User(id = userId, email = "test@example.com", password = "password")
        
        every { request.getHeader("Authorization") } returns "Bearer $token"
        every { jwtUtils.isTokenExpired(token) } returns false
        every { jwtUtils.isAccessToken(token) } returns true
        every { jwtUtils.getUserIdFromToken(token) } returns userId
        every { userRepository.findById(userId) } returns Optional.of(user)
        
        // Act
        filter.doFilterInternal(request, response, filterChain)
        
        // Assert
        verify { filterChain.doFilter(request, response) }
        
        // Check that authentication was set in security context
        val authentication = SecurityContextHolder.getContext().authentication
        assert(authentication != null)
        assert(authentication.principal == userId)
    }
} 