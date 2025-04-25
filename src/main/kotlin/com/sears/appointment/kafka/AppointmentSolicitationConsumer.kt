package com.sears.appointment.kafka

import com.sears.appointment.dto.AppointmentSolicitationKafkaMessage
import com.sears.appointment.services.interfaces.AppointmentSolicitationValidatorService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class AppointmentSolicitationConsumer(
    private val appointmentSolicitationValidatorService: AppointmentSolicitationValidatorService
) {
    private val logger = LoggerFactory.getLogger(AppointmentSolicitationConsumer::class.java)

    @KafkaListener(
        topics = ["\${kafka.topic.appointment-request}"],
        groupId = "\${spring.kafka.consumer.group-id}"
    )
    fun consumeAppointmentSolicitation(message: AppointmentSolicitationKafkaMessage) {
        logger.info("Received appointment solicitation message: {}", message)
        
        try {
            // Delegate all validation and processing logic to the service
            val result = appointmentSolicitationValidatorService.validateAndProcessSolicitation(
                message.appointmentSolicitationId
            )
            
            logger.info(
                "Successfully processed appointment solicitation: {}, new status: {}", 
                message.appointmentSolicitationId, 
                result.status
            )
        } catch (e: Exception) {
            logger.error(
                "Error processing appointment solicitation: {}", 
                message.appointmentSolicitationId, 
                e
            )
            
            // Try to reprocess the solicitation
            try {
                appointmentSolicitationValidatorService.reprocessSolicitation(message.appointmentSolicitationId)
            } catch (reprocessException: Exception) {
                logger.error(
                    "Failed to reprocess appointment solicitation: {}", 
                    message.appointmentSolicitationId, 
                    reprocessException
                )
            }
        }
    }
} 