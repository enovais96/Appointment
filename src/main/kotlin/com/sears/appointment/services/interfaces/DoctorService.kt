package com.sears.appointment.services.interfaces

import com.sears.appointment.dto.DoctorRequestDto
import com.sears.appointment.dto.DoctorResponseDto
import com.sears.appointment.dto.DoctorUpdateDto

interface DoctorService {
    fun createDoctor(doctorRequestDto: DoctorRequestDto): DoctorResponseDto
    fun getAllDoctors(): List<DoctorResponseDto>
    fun getDoctorById(id: String): DoctorResponseDto
    fun updateDoctor(id: String, doctorUpdateDto: DoctorUpdateDto): DoctorResponseDto
    fun deleteDoctor(id: String)
    fun getDoctorsBySpecialty(specialty: String): List<DoctorResponseDto>
} 