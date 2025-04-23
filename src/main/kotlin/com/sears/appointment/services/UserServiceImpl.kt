package com.sears.appointment.services

import com.sears.appointment.dto.UserRegistrationDto
import com.sears.appointment.dto.UserResponseDto
import com.sears.appointment.model.User
import com.sears.appointment.repositories.UserRepository
import com.sears.appointment.services.interfaces.UserService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
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

} 