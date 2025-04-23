package com.sears.appointment.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.util.Date

@Document(collection = "refresh_tokens")
data class RefreshToken(
    @Id
    val id: String? = null,
    val userId: String,
    val token: String,
    @Indexed(expireAfter = "0s")
    val expiryDate: Instant,
    val createdAt: Instant = Instant.now()
) 