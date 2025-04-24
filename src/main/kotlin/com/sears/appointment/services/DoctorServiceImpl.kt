package com.sears.appointment.services

import com.sears.appointment.dto.AvailabilitySlotDto
import com.sears.appointment.dto.DoctorRequestDto
import com.sears.appointment.dto.DoctorResponseDto
import com.sears.appointment.dto.DoctorUpdateDto
import com.sears.appointment.global.exceptions.ResourceNotFoundException
import com.sears.appointment.model.AvailabilitySlot
import com.sears.appointment.model.Doctor
import com.sears.appointment.repositories.DoctorRepository
import com.sears.appointment.services.interfaces.DoctorService
import org.springframework.stereotype.Service

@Service
class DoctorServiceImpl(private val doctorRepository: DoctorRepository) : DoctorService {

    override fun createDoctor(doctorRequestDto: DoctorRequestDto): DoctorResponseDto {
        val doctor = Doctor(
            name = doctorRequestDto.name,
            specialty = doctorRequestDto.specialty,
            availabilitySchedule = doctorRequestDto.availabilitySchedule.map { 
                AvailabilitySlot(it.dayOfWeek, it.startTime, it.endTime) 
            }
        )
        
        val savedDoctor = doctorRepository.save(doctor)
        return mapToResponseDto(savedDoctor)
    }

    override fun getAllDoctors(): List<DoctorResponseDto> {
        return doctorRepository.findAll().map { mapToResponseDto(it) }
    }

    override fun getDoctorById(id: String): DoctorResponseDto {
        val doctor = doctorRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Doctor not found with id: $id") }
        return mapToResponseDto(doctor)
    }

    override fun updateDoctor(id: String, doctorUpdateDto: DoctorUpdateDto): DoctorResponseDto {
        val existingDoctor = doctorRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Doctor not found with id: $id") }
        
        val updatedDoctor = existingDoctor.copy(
            name = doctorUpdateDto.name ?: existingDoctor.name,
            specialty = doctorUpdateDto.specialty ?: existingDoctor.specialty,
            availabilitySchedule = doctorUpdateDto.availabilitySchedule?.map { 
                AvailabilitySlot(it.dayOfWeek, it.startTime, it.endTime) 
            } ?: existingDoctor.availabilitySchedule
        )
        
        val savedDoctor = doctorRepository.save(updatedDoctor)
        return mapToResponseDto(savedDoctor)
    }

    override fun deleteDoctor(id: String) {
        if (!doctorRepository.existsById(id)) {
            throw ResourceNotFoundException("Doctor not found with id: $id")
        }
        doctorRepository.deleteById(id)
    }

    override fun getDoctorsBySpecialty(specialty: String): List<DoctorResponseDto> {
        return doctorRepository.findBySpecialty(specialty).map { mapToResponseDto(it) }
    }
    
    private fun mapToResponseDto(doctor: Doctor): DoctorResponseDto {
        return DoctorResponseDto(
            id = doctor.id!!,
            name = doctor.name,
            specialty = doctor.specialty,
            availabilitySchedule = doctor.availabilitySchedule.map { 
                AvailabilitySlotDto(it.dayOfWeek, it.startTime, it.endTime) 
            }
        )
    }
} 