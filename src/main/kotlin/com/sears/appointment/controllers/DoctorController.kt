package com.sears.appointment.controllers

import com.sears.appointment.dto.ApiReturn
import com.sears.appointment.dto.DoctorRequestDto
import com.sears.appointment.dto.DoctorResponseDto
import com.sears.appointment.dto.DoctorUpdateDto
import com.sears.appointment.services.interfaces.DoctorService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/doctors")
@Tag(name = "Doctor Management", description = "Endpoints for managing doctors")
@SecurityRequirement(name = "Bearer")
class DoctorController(private val doctorService: DoctorService) {

    @PostMapping
    @Operation(summary = "Register a new doctor", description = "Creates a new doctor with specialty and availability schedule")
    fun createDoctor(@Valid @RequestBody doctorRequestDto: DoctorRequestDto): ResponseEntity<ApiReturn<DoctorResponseDto>> {
        val createdDoctor = doctorService.createDoctor(doctorRequestDto)
        val apiResponse = ApiReturn(
            success = true,
            message = "Doctor created successfully",
            data = createdDoctor
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse)
    }

    @GetMapping
    @Operation(summary = "Get all doctors", description = "Retrieves all registered doctors")
    fun getAllDoctors(): ResponseEntity<ApiReturn<List<DoctorResponseDto>>> {
        val doctors = doctorService.getAllDoctors()
        val apiResponse = ApiReturn(
            success = true,
            message = "Doctors retrieved successfully",
            data = doctors
        )
        return ResponseEntity.ok(apiResponse)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get doctor by ID", description = "Retrieves a doctor by their ID")
    fun getDoctorById(@PathVariable id: String): ResponseEntity<ApiReturn<DoctorResponseDto>> {
        val doctor = doctorService.getDoctorById(id)
        val apiResponse = ApiReturn(
            success = true,
            message = "Doctor retrieved successfully",
            data = doctor
        )
        return ResponseEntity.ok(apiResponse)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update doctor", description = "Updates a doctor's information including availability schedule")
    fun updateDoctor(
        @PathVariable id: String,
        @Valid @RequestBody doctorUpdateDto: DoctorUpdateDto
    ): ResponseEntity<ApiReturn<DoctorResponseDto>> {
        val updatedDoctor = doctorService.updateDoctor(id, doctorUpdateDto)
        val apiResponse = ApiReturn(
            success = true,
            message = "Doctor updated successfully",
            data = updatedDoctor
        )
        return ResponseEntity.ok(apiResponse)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete doctor", description = "Deletes a doctor by their ID")
    fun deleteDoctor(@PathVariable id: String): ResponseEntity<ApiReturn<Nothing>> {
        doctorService.deleteDoctor(id)
        val apiResponse = ApiReturn<Nothing>(
            success = true,
            message = "Doctor deleted successfully",
            data = null
        )
        return ResponseEntity.ok(apiResponse)
    }

    @GetMapping("/specialty/{specialty}")
    @Operation(summary = "Get doctors by specialty", description = "Retrieves all doctors with a specific specialty")
    fun getDoctorsBySpecialty(@PathVariable specialty: String): ResponseEntity<ApiReturn<List<DoctorResponseDto>>> {
        val doctors = doctorService.getDoctorsBySpecialty(specialty)
        val apiResponse = ApiReturn(
            success = true,
            message = "Doctors retrieved successfully",
            data = doctors
        )
        return ResponseEntity.ok(apiResponse)
    }
} 