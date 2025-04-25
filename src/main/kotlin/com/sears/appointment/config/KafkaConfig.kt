package com.sears.appointment.config

import com.sears.appointment.dto.AppointmentSolicitationKafkaMessage
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.*
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.util.backoff.FixedBackOff
import java.time.Duration

@Configuration
class KafkaConfig {

    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Value("\${kafka.topic.appointment-request}")
    private lateinit var appointmentRequestTopic: String

    @Value("\${spring.kafka.consumer.group-id}")
    private lateinit var groupId: String

    // Producer configuration

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

    // Consumer configuration

    @Bean
    fun consumerConfigs(): Map<String, Any> {
        return mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to ErrorHandlingDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to ErrorHandlingDeserializer::class.java,
            ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS to JsonDeserializer::class.java,
            ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS to StringDeserializer::class.java,
            JsonDeserializer.VALUE_DEFAULT_TYPE to "com.sears.appointment.dto.AppointmentSolicitationKafkaMessage",
            JsonDeserializer.TRUSTED_PACKAGES to "com.sears.appointment.dto",
            JsonDeserializer.USE_TYPE_INFO_HEADERS to false,
            ConsumerConfig.GROUP_ID_CONFIG to groupId,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest"
        )
    }

    @Bean
    fun consumerFactory(): ConsumerFactory<String, AppointmentSolicitationKafkaMessage> {
        return DefaultKafkaConsumerFactory(consumerConfigs())
    }

    @Bean
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, AppointmentSolicitationKafkaMessage> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, AppointmentSolicitationKafkaMessage>()
        factory.consumerFactory = consumerFactory()

        // Configura uma política de retry simples com 2 tentativas e 1s de intervalo
        val backOff = FixedBackOff(1000L, 2L) // 1000 ms delay, 2 tentativas

        val errorHandler = DefaultErrorHandler(backOff)

        factory.setCommonErrorHandler(errorHandler)
        return factory
    }

    // Topic configuration

    @Bean
    fun appointmentRequestTopic(): NewTopic {
        return TopicBuilder.name(appointmentRequestTopic)
            .partitions(1)
            .replicas(1)
            .build()
    }
}