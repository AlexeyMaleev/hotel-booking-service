package com.example.hotels_app;

import com.example.hotels_app.repository.UserRepository;
import com.example.hotels_app.statistics.repository.HotelStatisticsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.stream.Stream;

public abstract class E2EBaseTest extends BaseTest {

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected HotelStatisticsRepository statisticsRepository;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected static final PostgreSQLContainer<?> postgres;
    protected static final MongoDBContainer mongo;
    protected static final KafkaContainer kafka;

    static {
        postgres = new PostgreSQLContainer<>("postgres:15-alpine")
                .withDatabaseName("hotels_test")
                .withUsername("test")
                .withPassword("test");
        mongo = new MongoDBContainer("mongo:6.0");
        kafka = new KafkaContainer(DockerImageName.parse("apache/kafka:3.7.2"));

        Stream.of(postgres, mongo, kafka).parallel().forEach(GenericContainer::start);
    }

    @DynamicPropertySource
    static void infraProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }
}
