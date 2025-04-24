package com.sears.appointment.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Kafka message for appointment solicitation processing")
data class AppointmentSolicitationKafkaMessage(
    @Schema(description = "Appointment solicitation ID", example = "61a2e0e3b54f6a23f0e66b4f")
    val appointmentSolicitationId: String
) 