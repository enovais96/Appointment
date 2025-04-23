package com.sears.appointment.services.interfaces

import com.sears.appointment.model.RefreshToken

interface RefreshTokenService {
    fun generateRefreshToken(userId: String): RefreshToken
    fun findByTokenAndUserId(token: String, userId: String): RefreshToken?
    fun verifyExpiration(token: RefreshToken): RefreshToken
    fun deleteByUserIdAndHashToken(userId: String, token: String)
} 