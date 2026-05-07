package com.example.hotels_app.kotlin.infra


import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext

class KotestContextInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    override fun initialize( context: ConfigurableApplicationContext) {

        // Прямо здесь пинаем контейнеры. Они стартуют ДО Flyway.
        val postgres = Containers.postgres
        val mongo = Containers.mongo
        val kafka = Containers.kafka

        // Прописываем свойства напрямую в окружение Spring
        TestPropertyValues.of(
            "spring.datasource.url=${postgres.jdbcUrl}",
            "spring.datasource.username=${postgres.username}",
            "spring.datasource.password=${postgres.password}",
            "spring.data.mongodb.uri=${mongo.replicaSetUrl}",
            "spring.kafka.bootstrap-servers=${kafka.bootstrapServers}"
        ).applyTo(context.environment)
    }

}