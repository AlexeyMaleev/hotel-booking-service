package io.kotest.provided

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.extensions.spring.SpringExtension

class ProjectConfig : AbstractProjectConfig() {
    // Этот экстеншн позволяет Kotest инжектить бины Spring в конструкторы тестов
    override fun extensions() = listOf(SpringExtension)

    // Это выполнится САМЫМ ПЕРВЫМ, до старта Spring
    override suspend fun beforeProject() {
        // Просто обращаемся к контейнерам, чтобы они стартанули
        // и прописали свои System.setProperty
        Containers.postgres
        Containers.mongo
        Containers.kafka
    }
}