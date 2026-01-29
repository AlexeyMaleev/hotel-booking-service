package com.example.hotels_app;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

//@Testcontainers
public abstract class PostgresBaseTest extends BaseTest{

    @MockitoBean
    protected KafkaTemplate<String, Object> kafkaTemplate;
/*
    @Container
    //@ServiceConnection
    protected static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("hotels_test")
            .withUsername("test")
            .withPassword("test")
            .waitingFor(Wait.forListeningPort());
*/
    protected static final PostgreSQLContainer<?> postgres;

    static{
        postgres = new PostgreSQLContainer<>("postgres:15-alpine")
                .withDatabaseName("hotels_test")
                .withUsername("test")
                .withPassword("test")
                .waitingFor(Wait.forListeningPort()); // Ждем готовности порта

        postgres.start(); // ГАРАНТИРУЕМ запуск до инициализации Spring
    }

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.datasource.hikari.connection-timeout", () -> 30000);
        registry.add("spring.datasource.hikari.initialization-fail-timeout", () -> 30000);
    }
}
