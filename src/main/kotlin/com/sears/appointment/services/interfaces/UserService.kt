package com.sears.appointment.services.interfaces

import com.sears.appointment.dto.UserRegistrationDto
import com.sears.appointment.dto.UserResponseDto

interface UserService {
    fun registerUser(userRegistrationDto: UserRegistrationDto): UserResponseDto
} 