package com.sears.appointment.services.interfaces

import com.sears.appointment.dto.LoginRequestDto
import com.sears.appointment.dto.TokenResponseDto
import com.sears.appointment.dto.UserRegistrationDto
import com.sears.appointment.dto.UserResponseDto

interface UserService {
    fun registerUser(userRegistrationDto: UserRegistrationDto): UserResponseDto
    fun loginUser(loginRequestDto: LoginRequestDto): TokenResponseDto
    fun refreshToken(refreshToken: String): TokenResponseDto
} 