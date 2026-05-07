package com.example.hotels_app.kotlin.e2e

import KafkaExtension
import MongoExtension
import PostgresExtension
import com.example.hotels_app.kotlin.infra.KotestContextInitializer
import com.example.hotels_app.model.request.UpsertUserRequest
import com.example.hotels_app.model.statistics.ActionType
import com.example.hotels_app.repository.UserRepository
import com.example.hotels_app.security.Role
import com.example.hotels_app.statistics.repository.HotelStatisticsRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import org.awaitility.kotlin.await
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Duration

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("kotest")
@ContextConfiguration(initializers = [KotestContextInitializer::class]) // Вот эта магия
class UserE2EKotestTest(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val userRepository: UserRepository,
    private val statisticsRepository: HotelStatisticsRepository
) : BehaviorSpec(){

    init {
        extensions(SpringExtension)
        //поднимаем все контейнеры
        extensions(PostgresExtension, KafkaExtension, MongoExtension)

        // Параметризация в Kotlin циклом по Enum
        Role.entries.forEach { currentRole ->

            Given("Регистрация пользователя с ролью $currentRole") {
                val userName = "E2E_User_${currentRole.name}"
                val request = UpsertUserRequest(
                    userName,
                    "pass123",
                    "test@mail.ru",
                    currentRole
                )

                // Чистим базы перед каждой итерацией
                userRepository.deleteAll()
                statisticsRepository.deleteAll().block()

                When("Запрос отправлен в API") {
                    mockMvc.perform(
                        post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                        .andExpect(status().isCreated)

                    Then("Данные должны долететь до MongoDB через Kafka") {
                        await.atMost(Duration.ofSeconds(5)).untilAsserted {
                            val count = statisticsRepository.count().block()
                            count shouldBe 1

                            val log = statisticsRepository.findAll().blockFirst()
                            log?.actionType shouldBe ActionType.REGISTRATION.name
                        }
                    }
                }
            }
        }
    }
}