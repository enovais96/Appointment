package com.sears.appointment.services.interfaces

import com.sears.appointment.model.RefreshToken
import org.apache.el.parser.Token
import java.time.Instant
import java.util.Optional

interface RefreshTokenService {
    fun generateRefreshToken(userId: String): RefreshToken
    fun findByTokenAndUserId(token: String, userId: String): Optional<RefreshToken>
    fun verifyExpiration(token: RefreshToken): RefreshToken
    fun deleteByUserIdAndHashToken(userId: String, token: String)
} 