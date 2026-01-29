package com.example.hotels_app;


import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;


public class FullStackBaseTest extends PostgresBaseTest{
/*
    @Container
    protected static MongoDBContainer mongo = new MongoDBContainer("mongo:6.0");

    @Container
    protected static KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("apache/kafka:3.7.2")

//            DockerImageName.parse("confluentinc/cp-kafka:6.2.0")
//                    .asCompatibleSubstituteFor("confluentinc/cp-kafka")
//    )
//            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("kafka-container")))
//            .withStartupTimeout(Duration.ofMinutes(2)

            );
*/
    protected static final MongoDBContainer mongo;
    protected static final KafkaContainer kafka;

    static{
        mongo = new MongoDBContainer("mongo:6.0");
        kafka = new KafkaContainer(DockerImageName.parse("apache/kafka:3.7.2"));

        // Запускаем всё вручную
        mongo.start();
        kafka.start();
    }

    @DynamicPropertySource
    static void infraProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }
}
