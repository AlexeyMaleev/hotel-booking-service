import io.kotest.core.listeners.BeforeSpecListener
import io.kotest.core.spec.Spec
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.kafka.KafkaContainer
import org.testcontainers.utility.DockerImageName

object Containers {
    // Явно указываем тип : PostgreSQLContainer<*>
    val postgres: PostgreSQLContainer<*> by lazy {
        PostgreSQLContainer("postgres:15-alpine").apply {
            withDatabaseName("hotels_test")
            start()
            // Устанавливаем свойства, которые ищет application-kotest.yml
            System.setProperty("KOTEST_DB_URL", jdbcUrl)
            System.setProperty("spring.datasource.username", username)
            System.setProperty("spring.datasource.password", password)
        }
    }

    // Явно указываем тип : MongoDBContainer
    val mongo: MongoDBContainer by lazy {
        MongoDBContainer("mongo:6.0").apply {
            start()
            //System.setProperty("spring.data.mongodb.uri", replicaSetUrl)
            // Указываем переменную для Монго
            System.setProperty("KOTEST_MONGO_URI", replicaSetUrl)
        }
    }

    // Явно указываем тип : KafkaContainer
    val kafka: KafkaContainer by lazy {
        KafkaContainer(DockerImageName.parse("apache/kafka:3.7.2")).apply {
            start()
            //System.setProperty("spring.kafka.bootstrap-servers", bootstrapServers)
            // Указываем переменную для Кафки
            System.setProperty("KOTEST_KAFKA_SERVERS", bootstrapServers)
        }
    }
}

val PostgresExtension = object : BeforeSpecListener {
    override suspend fun beforeSpec(spec: Spec) {
        Containers.postgres // Просто обращение к lazy-объекту запускает контейнер
    }
}

val KafkaExtension = object : BeforeSpecListener {
    override suspend fun beforeSpec(spec: Spec) {
        Containers.kafka
    }
}

val MongoExtension = object : BeforeSpecListener {
    override suspend fun beforeSpec(spec: Spec) {
        Containers.mongo // Активируем ленивую инициализацию Монго
    }
}