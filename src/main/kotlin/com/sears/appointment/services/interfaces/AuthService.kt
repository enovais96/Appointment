package com.sears.appointment.services.interfaces

import com.sears.appointment.dto.LoginRequestDto
import com.sears.appointment.dto.TokenResponseDto

interface AuthService {
    fun loginUser(loginRequestDto: LoginRequestDto): TokenResponseDto
    fun refreshToken(refreshToken: String): TokenResponseDto
} 