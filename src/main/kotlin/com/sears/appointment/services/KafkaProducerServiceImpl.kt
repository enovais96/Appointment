package com.sears.appointment.services

import com.sears.appointment.dto.AppointmentSolicitationKafkaMessage
import com.sears.appointment.services.interfaces.KafkaProducerService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class KafkaProducerServiceImpl(
    private val kafkaTemplate: KafkaTemplate<String, AppointmentSolicitationKafkaMessage>
) : KafkaProducerService {

    private val logger = LoggerFactory.getLogger(KafkaProducerServiceImpl::class.java)

    @Value("\${kafka.topic.appointment-request}")
    private lateinit var appointmentRequestTopic: String

    override fun sendAppointmentSolicitationMessage(message: AppointmentSolicitationKafkaMessage) {
        logger.info("Sending appointment solicitation message: {}", message)
        
        kafkaTemplate.send(appointmentRequestTopic, message)
            .whenComplete { result, ex ->
                if (ex == null) {
                    logger.info(
                        "Sent message=[{}] with offset=[{}] to topic=[{}]",
                        message,
                        result?.recordMetadata?.offset(),
                        result?.recordMetadata?.topic()
                    )
                } else {
                    logger.error("Unable to send message=[${message}] due to: ${ex.message}", ex)
                }
            }
    }
} 