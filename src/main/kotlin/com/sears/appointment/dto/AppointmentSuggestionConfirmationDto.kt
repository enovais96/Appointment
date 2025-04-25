package com.sears.appointment.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull

@Schema(description = "DTO for confirming or rejecting a suggested appointment")
data class AppointmentSuggestionConfirmationDto(
    @field:NotNull(message = "Accept flag is required")
    @Schema(description = "Whether the patient accepts the suggested appointment time", example = "true")
    val accept: Boolean
) 