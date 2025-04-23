package com.sears.appointment.services

import com.sears.appointment.model.RefreshToken
import com.sears.appointment.repositories.RefreshTokenRepository
import com.sears.appointment.services.interfaces.RefreshTokenService
import com.sears.appointment.utils.JwtUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class RefreshTokenServiceImpl(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtUtils: JwtUtils
) : RefreshTokenService {

    @Value("\${jwt.refresh-token.expiration}")
    private var jwtRefreshTokenExpiration: Long = 0

    override fun generateRefreshToken(userId: String): RefreshToken {
        val token = jwtUtils.generateRefreshToken(userId, jwtRefreshTokenExpiration)

        val refreshToken = RefreshToken(
            userId = userId,
            token = token,
            expiryDate = Instant.now().plusMillis(jwtRefreshTokenExpiration)
        )

        return refreshTokenRepository.save(refreshToken)
    }

    override fun findByTokenAndUserId(token: String, userId: String): RefreshToken? {
        return refreshTokenRepository.findByTokenAndUserId(token, userId)
    }

    override fun verifyExpiration(token: RefreshToken): RefreshToken {
        if (token.expiryDate.isBefore(Instant.now())) {
            refreshTokenRepository.delete(token)
            throw IllegalArgumentException("Refresh token was expired. Please make a new signin request")
        }
        return token
    }

    override fun deleteByUserIdAndHashToken(userId: String, token: String) {
        refreshTokenRepository.deleteByUserIdAndToken(userId, token)
    }
} 