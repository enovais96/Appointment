package com.sears.appointment.utils

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtUtils {

    @Value("\${jwt.secret}")
    private lateinit var jwtSecretString: String

    @Value("\${jwt.access-token.expiration}")
    private var jwtAccessTokenExpiration: Long = 0

    private val jwtSecret: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtSecretString.toByteArray())
    }

    fun generateAccessToken(userId: String): String {
        return generateToken(userId, "access", jwtAccessTokenExpiration)
    }

    fun generateRefreshToken(userId: String, jwtRefreshTokenExpiration: Long): String {
        return generateToken(userId, "refresh", jwtRefreshTokenExpiration)
    }

    private fun generateToken(userId: String, type: String, expiration: Long): String {
        val now = System.currentTimeMillis()

        return Jwts.builder()
            .subject(userId)
            .claim("type", type)
            .issuedAt(Date(now))
            .expiration(Date(now + expiration))
            .signWith(jwtSecret, Jwts.SIG.HS256)
            .compact()
    }

    fun validateRefreshToken(token: String): Boolean {
        val claims = getAllClaimsFromToken(token)
        val tokenType = claims["type"] as? String ?: return false
        return tokenType == "refresh"
    }

    fun getUserIdFromToken(token: String): String {
        val claims = getAllClaimsFromToken(token)
        return claims.subject
    }

    fun validateToken(token: String, userDetails: UserDetails): Boolean {
        val userId = getUserIdFromToken(token)
        return userId == userDetails.username && !isTokenExpired(token)
    }

    fun isTokenExpired(token: String): Boolean {
        val expiration = getExpirationDateFromToken(token)
        return expiration.before(Date())
    }

    fun getExpirationDateFromToken(token: String): Date {
        val claims = getAllClaimsFromToken(token)
        return claims.expiration
    }

    private fun getAllClaimsFromToken(token: String): Claims {
        val rawToken = if(token.startsWith("Bearer ")) {
            token.removePrefix("Bearer ")
        } else token

        return Jwts.parser()
            .verifyWith(jwtSecret)
            .build()
            .parseSignedClaims(rawToken)
            .payload
    }
} 