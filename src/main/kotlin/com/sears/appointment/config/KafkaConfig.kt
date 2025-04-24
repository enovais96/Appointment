package com.sears.appointment.config

import com.sears.appointment.dto.AppointmentSolicitationKafkaMessage
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
class KafkaConfig {

    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String
    
    @Value("\${kafka.topic.appointment-request}")
    private lateinit var appointmentRequestTopic: String
    
    @Value("\${kafka.topic.appointment-response}")
    private lateinit var appointmentResponseTopic: String
    
    @Value("\${kafka.topic.appointment-confirmation}")
    private lateinit var appointmentConfirmationTopic: String

    @Bean
    fun producerConfigs(): Map<String, Any> {
        return mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
            JsonSerializer.TYPE_MAPPINGS to "appointment:com.sears.appointment.dto.AppointmentSolicitationKafkaMessage"
        )
    }

    @Bean
    fun producerFactory(): ProducerFactory<String, AppointmentSolicitationKafkaMessage> {
        return DefaultKafkaProducerFactory(producerConfigs())
    }

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, AppointmentSolicitationKafkaMessage> {
        return KafkaTemplate(producerFactory())
    }

    @Bean
    fun appointmentRequestTopic(): NewTopic {
        return TopicBuilder.name(appointmentRequestTopic)
            .partitions(1)
            .replicas(1)
            .build()
    }

    @Bean
    fun appointmentResponseTopic(): NewTopic {
        return TopicBuilder.name(appointmentResponseTopic)
            .partitions(1)
            .replicas(1)
            .build()
    }

    @Bean
    fun appointmentConfirmationTopic(): NewTopic {
        return TopicBuilder.name(appointmentConfirmationTopic)
            .partitions(1)
            .replicas(1)
            .build()
    }
} 