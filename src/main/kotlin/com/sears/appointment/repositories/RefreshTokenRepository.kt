package com.sears.appointment.repositories

import com.sears.appointment.model.RefreshToken
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface RefreshTokenRepository : MongoRepository<RefreshToken, String> {
    fun findByTokenAndUserId(token: String, userId: String): Optional<RefreshToken>
    fun deleteByUserIdAndToken(userId: String, token: String)
} 