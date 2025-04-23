package com.sears.appointment.services

import com.sears.appointment.model.RefreshToken
import com.sears.appointment.repositories.RefreshTokenRepository
import com.sears.appointment.utils.JwtUtils
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.test.util.ReflectionTestUtils
import java.time.Instant

class RefreshTokenServiceImplTest {

    private val refreshTokenRepository: RefreshTokenRepository = mockk()
    private val jwtUtils: JwtUtils = mockk()
    private val refreshTokenService = RefreshTokenServiceImpl(refreshTokenRepository, jwtUtils)
    private val refreshTokenExpiration = 1296000000L // 15 days in milliseconds

    @BeforeEach
    fun setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "jwtRefreshTokenExpiration", refreshTokenExpiration)
    }

    @Test
    fun `should generate refresh token successfully`() {
        // Arrange
        val userId = "userId123"
        val tokenValue = "generated-token-value"
        val refreshToken = RefreshToken(
            id = "tokenId",
            userId = userId,
            token = tokenValue,
            expiryDate = Instant.now().plusMillis(refreshTokenExpiration)
        )

        every { jwtUtils.generateRefreshToken(userId, refreshTokenExpiration) } returns tokenValue
        
        val tokenSlot = slot<RefreshToken>()
        every { refreshTokenRepository.save(capture(tokenSlot)) } returns refreshToken

        // Act
        val result = refreshTokenService.generateRefreshToken(userId)

        // Assert
        assertEquals(userId, result.userId)
        assertEquals(tokenValue, result.token)
        
        verify { jwtUtils.generateRefreshToken(userId, refreshTokenExpiration) }
        verify { refreshTokenRepository.save(any()) }
        
        val savedToken = tokenSlot.captured
        assertEquals(userId, savedToken.userId)
        assertEquals(tokenValue, savedToken.token)
    }

    @Test
    fun `should find token by token value and user id`() {
        // Arrange
        val userId = "userId123"
        val tokenValue = "token-value"
        val refreshToken = RefreshToken(
            id = "tokenId",
            userId = userId,
            token = tokenValue,
            expiryDate = Instant.now().plusMillis(refreshTokenExpiration)
        )

        every { refreshTokenRepository.findByTokenAndUserId(tokenValue, userId) } returns refreshToken

        // Act
        val result = refreshTokenService.findByTokenAndUserId(tokenValue, userId)

        // Assert
        assertSame(refreshToken, result)
        
        verify { refreshTokenRepository.findByTokenAndUserId(tokenValue, userId) }
    }

    @Test
    fun `should verify token is not expired`() {
        // Arrange
        val userId = "userId123"
        val tokenValue = "token-value"
        val expiryDate = Instant.now().plusMillis(refreshTokenExpiration)
        val refreshToken = RefreshToken(
            id = "tokenId",
            userId = userId,
            token = tokenValue,
            expiryDate = expiryDate
        )

        // Act
        val result = refreshTokenService.verifyExpiration(refreshToken)

        // Assert
        assertSame(refreshToken, result)
        verify(exactly = 0) { refreshTokenRepository.delete(any()) }
    }

    @Test
    fun `should throw exception when token is expired`() {
        // Arrange
        val userId = "userId123"
        val tokenValue = "token-value"
        val expiryDate = Instant.now().minusSeconds(10) // Expired
        val refreshToken = RefreshToken(
            id = "tokenId",
            userId = userId,
            token = tokenValue,
            expiryDate = expiryDate
        )
        
        every { refreshTokenRepository.delete(refreshToken) } just runs

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            refreshTokenService.verifyExpiration(refreshToken)
        }

        assertEquals("Refresh token was expired. Please make a new signin request", exception.message)
        
        verify { refreshTokenRepository.delete(refreshToken) }
    }

    @Test
    fun `should delete token by user id and token value`() {
        // Arrange
        val userId = "userId123"
        val tokenValue = "token-value"
        
        every { refreshTokenRepository.deleteByUserIdAndToken(userId, tokenValue) } just runs

        // Act
        refreshTokenService.deleteByUserIdAndHashToken(userId, tokenValue)

        // Assert
        verify { refreshTokenRepository.deleteByUserIdAndToken(userId, tokenValue) }
    }
} 