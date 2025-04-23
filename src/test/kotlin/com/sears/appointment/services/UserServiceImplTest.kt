package com.sears.appointment.services

import com.sears.appointment.dto.UserRegistrationDto
import com.sears.appointment.model.User
import com.sears.appointment.repositories.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.crypto.password.PasswordEncoder

class UserServiceImplTest {

    private val userRepository: UserRepository = mockk()
    private val passwordEncoder: PasswordEncoder = mockk()
    private val userService = UserServiceImpl(userRepository, passwordEncoder)

    @Test
    fun `should register user successfully`() {
        // Arrange
        val registrationDto = UserRegistrationDto(
            email = "test@example.com",
            password = "Test@123"
        )

        val encodedPassword = "encoded-password"
        val userId = "userId123"
        
        every { userRepository.existsByEmail(registrationDto.email) } returns false
        every { passwordEncoder.encode(registrationDto.password) } returns encodedPassword
        
        val userSlot = slot<User>()
        
        val savedUser = User(
            id = userId,
            email = registrationDto.email,
            password = encodedPassword
        )
        
        every { userRepository.save(capture(userSlot)) } returns savedUser

        // Act
        val result = userService.registerUser(registrationDto)

        // Assert
        assertEquals(userId, result.id)
        assertEquals(registrationDto.email, result.email)
        
        verify { userRepository.existsByEmail(registrationDto.email) }
        verify { passwordEncoder.encode(registrationDto.password) }
        verify { userRepository.save(any()) }
        
        val capturedUser = userSlot.captured
        assertEquals(registrationDto.email, capturedUser.email)
        assertEquals(encodedPassword, capturedUser.password)
    }

    @Test
    fun `should throw exception when email already exists`() {
        // Arrange
        val registrationDto = UserRegistrationDto(
            email = "existing@example.com",
            password = "Test@123"
        )

        every { userRepository.existsByEmail(registrationDto.email) } returns true

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            userService.registerUser(registrationDto)
        }
        
        assertEquals("User with email ${registrationDto.email} already exists", exception.message)
        
        verify { userRepository.existsByEmail(registrationDto.email) }
        verify(exactly = 0) { passwordEncoder.encode(any()) }
        verify(exactly = 0) { userRepository.save(any()) }
    }
} 