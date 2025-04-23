package com.sears.appointment.services

import com.sears.appointment.dto.LoginRequestDto
import com.sears.appointment.model.RefreshToken
import com.sears.appointment.model.User
import com.sears.appointment.repositories.UserRepository
import com.sears.appointment.services.interfaces.RefreshTokenService
import com.sears.appointment.utils.JwtUtils
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.Instant
import java.util.Optional

class AuthServiceImplTest {

    private val userRepository: UserRepository = mockk()
    private val passwordEncoder: PasswordEncoder = mockk()
    private val jwtUtils: JwtUtils = mockk()
    private val refreshTokenService: RefreshTokenService = mockk()
    private val authService = AuthServiceImpl(userRepository, passwordEncoder, jwtUtils, refreshTokenService)

    @Test
    fun `should login user successfully`() {
        // Arrange
        val loginRequestDto = LoginRequestDto(
            email = "test@example.com",
            password = "Test@123"
        )

        val userId = "userId123"
        val user = User(
            id = userId,
            email = loginRequestDto.email,
            password = "encoded-password"
        )

        val accessToken = "mock-access-token"
        val refreshTokenValue = "mock-refresh-token"
        val refreshToken = RefreshToken(
            id = "refreshId",
            userId = userId,
            token = refreshTokenValue,
            expiryDate = Instant.now().plusSeconds(900)
        )

        every { userRepository.findByEmail(loginRequestDto.email) } returns user
        every { passwordEncoder.matches(loginRequestDto.password, user.password) } returns true
        every { jwtUtils.generateAccessToken(userId) } returns accessToken
        every { refreshTokenService.generateRefreshToken(userId) } returns refreshToken

        // Act
        val result = authService.loginUser(loginRequestDto)

        // Assert
        assertEquals(accessToken, result.accessToken)
        assertEquals(refreshTokenValue, result.refreshToken)

        verify { userRepository.findByEmail(loginRequestDto.email) }
        verify { passwordEncoder.matches(loginRequestDto.password, user.password) }
        verify { jwtUtils.generateAccessToken(userId) }
        verify { refreshTokenService.generateRefreshToken(userId) }
    }

    @Test
    fun `should throw exception when email not found`() {
        // Arrange
        val loginRequestDto = LoginRequestDto(
            email = "nonexistent@example.com",
            password = "Test@123"
        )

        every { userRepository.findByEmail(loginRequestDto.email) } returns null

        // Act & Assert
        val exception = assertThrows<BadCredentialsException> {
            authService.loginUser(loginRequestDto)
        }

        assertEquals("Invalid email or password", exception.message)

        verify { userRepository.findByEmail(loginRequestDto.email) }
        verify(exactly = 0) { passwordEncoder.matches(any(), any()) }
    }

    @Test
    fun `should throw exception when password doesn't match`() {
        // Arrange
        val loginRequestDto = LoginRequestDto(
            email = "test@example.com",
            password = "WrongPassword"
        )

        val user = User(
            id = "userId123",
            email = loginRequestDto.email,
            password = "encoded-password"
        )

        every { userRepository.findByEmail(loginRequestDto.email) } returns user
        every { passwordEncoder.matches(loginRequestDto.password, user.password) } returns false

        // Act & Assert
        val exception = assertThrows<BadCredentialsException> {
            authService.loginUser(loginRequestDto)
        }

        assertEquals("Invalid email or password", exception.message)

        verify { userRepository.findByEmail(loginRequestDto.email) }
        verify { passwordEncoder.matches(loginRequestDto.password, user.password) }
        verify(exactly = 0) { jwtUtils.generateAccessToken(any()) }
    }

    @Test
    fun `should refresh token successfully`() {
        // Arrange
        val refreshTokenValue = "valid-refresh-token"
        val userId = "userId123"
        val user = User(
            id = userId,
            email = "test@example.com",
            password = "encoded-password"
        )
        val existingRefreshToken = RefreshToken(
            id = "refreshId",
            userId = userId,
            token = refreshTokenValue,
            expiryDate = Instant.now().plusSeconds(900)
        )
        val newAccessToken = "new-access-token"
        val newRefreshToken = RefreshToken(
            id = "newRefreshId",
            userId = userId,
            token = "new-refresh-token",
            expiryDate = Instant.now().plusSeconds(900)
        )

        every { jwtUtils.validateRefreshToken(refreshTokenValue) } returns true
        every { jwtUtils.getUserIdFromToken(refreshTokenValue) } returns userId
        every { userRepository.findById(userId) } returns Optional.of(user)
        every { refreshTokenService.findByTokenAndUserId(refreshTokenValue, userId) } returns existingRefreshToken
        every { refreshTokenService.verifyExpiration(existingRefreshToken) } returns existingRefreshToken
        every { refreshTokenService.deleteByUserIdAndHashToken(userId, refreshTokenValue) } just runs
        every { jwtUtils.generateAccessToken(userId) } returns newAccessToken
        every { refreshTokenService.generateRefreshToken(userId) } returns newRefreshToken

        // Act
        val result = authService.refreshToken(refreshTokenValue)

        // Assert
        assertEquals(newAccessToken, result.accessToken)
        assertEquals(newRefreshToken.token, result.refreshToken)

        verify { jwtUtils.validateRefreshToken(refreshTokenValue) }
        verify { jwtUtils.getUserIdFromToken(refreshTokenValue) }
        verify { userRepository.findById(userId) }
        verify { refreshTokenService.findByTokenAndUserId(refreshTokenValue, userId) }
        verify { refreshTokenService.verifyExpiration(existingRefreshToken) }
        verify { refreshTokenService.deleteByUserIdAndHashToken(userId, refreshTokenValue) }
        verify { jwtUtils.generateAccessToken(userId) }
        verify { refreshTokenService.generateRefreshToken(userId) }
    }

    @Test
    fun `should throw exception when refresh token is invalid`() {
        // Arrange
        val refreshTokenValue = "invalid-refresh-token"

        every { jwtUtils.validateRefreshToken(refreshTokenValue) } returns false

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            authService.refreshToken(refreshTokenValue)
        }

        assertEquals("Invalid refresh token", exception.message)

        verify { jwtUtils.validateRefreshToken(refreshTokenValue) }
        verify(exactly = 0) { jwtUtils.getUserIdFromToken(any()) }
    }
} 