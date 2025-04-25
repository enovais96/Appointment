package com.sears.appointment.repositories

import com.sears.appointment.model.AppointmentSolicitation
import com.sears.appointment.model.AppointmentStatus
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDate

@DataMongoTest
@Testcontainers
class AppointmentSolicitationRepositoryTest {

    companion object {
        @Container
        private val mongoDBContainer = MongoDBContainer("mongo:5.0.14")

        @JvmStatic
        @DynamicPropertySource
        fun setProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.mongodb.uri") { mongoDBContainer.replicaSetUrl }
        }
    }

    @Autowired
    private lateinit var appointmentSolicitationRepository: AppointmentSolicitationRepository

    @BeforeEach
    fun setUp() {
        appointmentSolicitationRepository.deleteAll()
        
        // Create test data
        val solicitations = listOf(
            createTestSolicitation("id1", AppointmentStatus.PENDING),
            createTestSolicitation("id2", AppointmentStatus.SUGGESTED),
            createTestSolicitation("id3", AppointmentStatus.SUGGESTED),
            createTestSolicitation("id4", AppointmentStatus.SUGGESTED),
            createTestSolicitation("id5", AppointmentStatus.CONFIRMED),
            createTestSolicitation("id6", AppointmentStatus.REJECTED)
        )
        
        appointmentSolicitationRepository.saveAll(solicitations)
    }
    
    @Test
    fun `findByStatus should return all solicitations with matching status`() {
        // Act
        val suggestedSolicitations = appointmentSolicitationRepository.findByStatus(AppointmentStatus.SUGGESTED)
        
        // Assert
        assertEquals(3, suggestedSolicitations.size)
        
        suggestedSolicitations.forEach { 
            assertEquals(AppointmentStatus.SUGGESTED, it.status)
        }
    }
    
    @Test
    fun `findByStatus with pageable should return paginated solicitations with matching status`() {
        // Arrange
        val pageable = PageRequest.of(0, 2, Sort.by("createdAt").descending())
        
        // Act
        val suggestedSolicitations = appointmentSolicitationRepository.findByStatus(
            AppointmentStatus.SUGGESTED, 
            pageable
        )
        
        // Assert
        assertEquals(3, suggestedSolicitations.totalElements)
        assertEquals(2, suggestedSolicitations.content.size)
        assertEquals(0, suggestedSolicitations.number)
        assertEquals(2, suggestedSolicitations.pageable.pageSize)
        
        suggestedSolicitations.content.forEach { 
            assertEquals(AppointmentStatus.SUGGESTED, it.status)
        }
    }
    
    @Test
    fun `findByIdAndStatus should return solicitation with matching id and status`() {
        // Act
        val solicitation = appointmentSolicitationRepository.findByIdAndStatus("id3", AppointmentStatus.SUGGESTED)
        
        // Assert
        assertNotNull(solicitation)
        assertEquals("id3", solicitation?.id)
        assertEquals(AppointmentStatus.SUGGESTED, solicitation?.status)
    }
    
    @Test
    fun `findByIdAndStatus should return null when id exists but status doesn't match`() {
        // Act
        val solicitation = appointmentSolicitationRepository.findByIdAndStatus("id1", AppointmentStatus.SUGGESTED)
        
        // Assert
        assertNull(solicitation)
    }
    
    @Test
    fun `findByIdAndStatus should return null when id doesn't exist`() {
        // Act
        val solicitation = appointmentSolicitationRepository.findByIdAndStatus("non-existent-id", AppointmentStatus.SUGGESTED)
        
        // Assert
        assertNull(solicitation)
    }
    
    private fun createTestSolicitation(id: String, status: AppointmentStatus): AppointmentSolicitation {
        return AppointmentSolicitation(
            id = id,
            patientName = "Test Patient",
            patientAge = 30,
            patientPhone = "123-456-7890",
            patientEmail = "test@example.com",
            specialty = "Orthopedics",
            requestedDate = LocalDate.now(),
            requestedTime = "09:00",
            status = status,
            doctorId = if (status != AppointmentStatus.PENDING) "doctor-id" else null,
            suggestedDate = if (status == AppointmentStatus.SUGGESTED) LocalDate.now().plusDays(1) else null,
            suggestedTime = if (status == AppointmentStatus.SUGGESTED) "14:00" else null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }
} 