package com.example.hotels_app.kotlin.controller

import PostgresExtension
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import com.example.hotels_app.kotlin.infra.KotestContextInitializer
import com.example.hotels_app.model.request.UpsertUserRequest
import com.example.hotels_app.security.Role
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.kotest.datatest.withData
import io.kotest.extensions.spring.SpringExtension
import io.mockk.coVerify
import org.springframework.http.MediaType
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("kotest")
@ContextConfiguration(initializers = [KotestContextInitializer::class]) // Вот эта магия
class UserControllerKotestTest(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
) : StringSpec() {

    @MockkBean(relaxed = true)
    lateinit var kafkaTemplate: KafkaTemplate<String, Any>

    // 2. Всю логику тестов переносим в блок init
    init {
        extensions(SpringExtension)

        // Подключаем только Postgres для этого теста
        extensions(PostgresExtension)

        "Эндпоинт получения пользователей должен возвращать 200" {
            val result = mockMvc.perform(get("/api/v1/users")
                .with(user("admin").roles("ADMIN")))
                .andReturn().response

            result.status shouldBe 200
        }

        "Создание пользователя должно отправлять сообщение в Kafka" {
            val request = UpsertUserRequest("Alexey", "pass", "mail@mail.ru", Role.ADMIN)

            mockMvc.perform(
                post("/api/v1/users")
                    .with(user("admin").roles("ADMIN"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)

            // Проверка через MockK
            coVerify(exactly = 1) {
                // Явно указываем, что первый аргумент — любая строка, а второй — любой объект
                kafkaTemplate.send(any<String>(), any())
            }
        }

        // Параметризованный блок (Data-Driven)
        //"Негативные сценарии создания пользователя"
         withData(
            UpsertUserRequest("", "pass", "mail@mail.ru", Role.USER) to "пустое имя",
            UpsertUserRequest("Alex", "", "mail@mail.ru", Role.USER) to "пустой пароль",
            UpsertUserRequest("Alex", "pass", "not-an-email", Role.USER) to "кривой email"
        ) { (request, description) ->
            mockMvc.perform(post("/api/v1/users")
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest)
        }
    }

}
