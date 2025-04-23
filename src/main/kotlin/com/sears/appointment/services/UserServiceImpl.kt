package com.sears.appointment.services

import com.sears.appointment.dto.LoginRequestDto
import com.sears.appointment.dto.TokenResponseDto
import com.sears.appointment.dto.UserRegistrationDto
import com.sears.appointment.dto.UserResponseDto
import com.sears.appointment.model.User
import com.sears.appointment.repositories.UserRepository
import com.sears.appointment.services.interfaces.RefreshTokenService
import com.sears.appointment.services.interfaces.UserService
import com.sears.appointment.utils.JwtUtils
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtils: JwtUtils,
    private val refreshTokenService: RefreshTokenService
) : UserService {

    @Transactional
    override fun registerUser(userRegistrationDto: UserRegistrationDto): UserResponseDto {
        if (userRepository.existsByEmail(userRegistrationDto.email)) {
            throw IllegalArgumentException("User with email ${userRegistrationDto.email} already exists")
        }

        val user = User(
            email = userRegistrationDto.email,
            password = passwordEncoder.encode(userRegistrationDto.password)
        )

        val savedUser = userRepository.save(user)

        return UserResponseDto(
            id = savedUser.id,
            email = savedUser.email
        )
    }

    @Transactional
    override fun loginUser(loginRequestDto: LoginRequestDto): TokenResponseDto {
        val user = userRepository.findByEmail(loginRequestDto.email)
            ?: throw BadCredentialsException("Invalid email or password")

        if (!passwordEncoder.matches(loginRequestDto.password, user.password)) {
            throw BadCredentialsException("Invalid email or password")
        }

        val userId = user.id ?: throw IllegalStateException("User ID is null")
        val accessToken = jwtUtils.generateAccessToken(userId)
        val refreshToken = refreshTokenService.generateRefreshToken(userId)

        return TokenResponseDto(
            accessToken = accessToken,
            refreshToken = refreshToken.token
        )
    }

    @Transactional
    override fun refreshToken(refreshToken: String): TokenResponseDto {
        if(!jwtUtils.validateRefreshToken(refreshToken)) {
            throw IllegalArgumentException("Invalid refresh token")
        }

        val userId = jwtUtils.getUserIdFromToken(refreshToken)

        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("Invalid refresh token") }

        val refreshTokenObj = refreshTokenService.findByTokenAndUserId(refreshToken, user.id.toString())
            .orElseThrow { IllegalArgumentException("Invalid refresh token" ) }
        
        refreshTokenService.verifyExpiration(refreshTokenObj)

        refreshTokenService.deleteByUserIdAndHashToken(userId, refreshToken)

        val accessToken = jwtUtils.generateAccessToken(userId)
        val refreshToken = refreshTokenService.generateRefreshToken(userId)
        
        return TokenResponseDto(
            accessToken = accessToken,
            refreshToken = refreshToken.token
        )
    }
} 