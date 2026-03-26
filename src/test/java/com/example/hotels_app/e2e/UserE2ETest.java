package com.example.hotels_app.e2e;

import com.example.hotels_app.E2EBaseTest;
import com.example.hotels_app.FullStackBaseTest;
import com.example.hotels_app.entity.User;
import com.example.hotels_app.model.request.UpsertUserRequest;
import com.example.hotels_app.model.statistics.ActionType;
import com.example.hotels_app.repository.UserRepository;
import com.example.hotels_app.security.Role;
import com.example.hotels_app.statistics.repository.HotelStatisticsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.web.servlet.MockMvc;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class UserE2ETest extends E2EBaseTest {
/*
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

 */

    /*
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HotelStatisticsRepository statisticsRepository;

     */


    // 2. ГЛАВНОЕ: Переопределяем мок реальным бином

    @BeforeEach
    void setUp() {
        userRepository.deleteAll(); // Чистим Postgres
        statisticsRepository.deleteAll().block(); // Чистим реактивную Mongo
        //Mockito.reset(kafkaTemplate); // Сбрасываем счетчик вызовов мока Кафки
    }


    @ParameterizedTest
    @EnumSource(Role.class)
    @DisplayName("""
    Given valid user registration data
    When authorized request to create new user is sent via API
    Then status is 201 Created
     and user record is persisted in PostgreSQL
     and "USER_CREATED" event is published to Kafka
     and statistics listener saves record to MongoDB
    """
    )
    void whenCreateUser_thenStatisticsSavedInMongo(Role role) throws Exception {
        // 1. Данные запроса
        String userName = "User_USER";
        String userEmail = "user@example.com";

        UpsertUserRequest request = new UpsertUserRequest(userName, "pass123", userEmail, role);

        // 2. Отправляем запрос через MockMvc (имитируем реальный вызов API)
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // 3. Get generated User from PostgreSQL
        User savedUser = userRepository.findByName(userName)
                .orElseThrow(() -> new AssertionError("User not found in database: " + userName));

        // 5. Verify MongoDB Integration (Async via Awaitility)
        await()
                .atMost(5, SECONDS)
                .untilAsserted(() -> {
                    Long count = statisticsRepository.count().block();
                    assertEquals(1, count, "Expected exactly 1 record in MongoDB statistics");

                    var logs = statisticsRepository.findAll().collectList().block();
                    //System.out.println("LOGS IN MONGO: " + logs);
                    boolean isLogPresent = logs.stream().anyMatch(log ->
                            log.getUserId().equals(savedUser.getId()) &&
                                    ActionType.REGISTRATION.name().equals(log.getActionType()));

                    assertTrue(isLogPresent, "User creation log not found in MongoDB for User ID: " + savedUser.getId());
                });
    }
}