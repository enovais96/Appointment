package com.sears.appointment.repositories

import com.sears.appointment.model.RefreshToken
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface RefreshTokenRepository : MongoRepository<RefreshToken, String> {
    fun findByTokenAndUserId(token: String, userId: String): RefreshToken?
    fun deleteByUserIdAndToken(userId: String, token: String)
} 