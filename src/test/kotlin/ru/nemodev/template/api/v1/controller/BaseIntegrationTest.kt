package ru.nemodev.template.api.v1.controller

import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import org.wiremock.integrations.testcontainers.WireMockContainer
import java.io.File

//@SpringBootTest(
//    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
//    classes = [ru.nemodev.template.Application::class]
//)
//@ActiveProfiles("it")
open class BaseIntegrationTest {

//    @Autowired
    protected lateinit var webClient: WebTestClient

    companion object {

        private val postgresContainer = PostgreSQLContainer(DockerImageName.parse("postgres:latest"))

        private val kafkaContainer = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest")).also {
            it.withKraft() // запуск без zookeeper
        }

        private val wiremockContainer = WireMockContainer(WireMockContainer.WIREMOCK_2_LATEST).also {
            // включаем глобальную обработку ответов шаблонов
            it.withCliArg("-global-response-templating")

            val wiremockDir = "src/test/resources/wiremock"
            // указываем файлы заглушек
            File("$wiremockDir/mappings")
                .walk()
                .filter { file -> file.isFile }
                .forEach { file ->
                    it.withMappingFromResource("wiremock/mappings/${file.name}")
                }
            // указываем файлы ответов заглушек при необходимости
            File("$wiremockDir/files")
                .walk()
                .filter { file -> file.isFile }
                .forEach { file ->
                    it.withFileFromResource(file.name, "wiremock/files/${file.name}")
                }
        }

//        @BeforeAll
//        @JvmStatic
        fun beforeStart() {
            // Запуск контейнеров вручную (не через аннотацию @Container)
            // Это позволяет избежать ошибки старта контейнера Kafka при наличии нескольких тестовых классов
            postgresContainer.start()
            kafkaContainer.start()
            wiremockContainer.start()

            System.setProperty("DATABASE_URL", "${postgresContainer.host}:${postgresContainer.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT)}")
            System.setProperty("DATABASE_NAME", postgresContainer.databaseName)
            System.setProperty("DATABASE_USERNAME", postgresContainer.username)
            System.setProperty("DATABASE_PASSWORD", postgresContainer.password)

            System.setProperty("KAFKA_URLS", "${kafkaContainer.host}:${kafkaContainer.getMappedPort(KafkaContainer.KAFKA_PORT)}")

            System.setProperty("WET_CLINIC_URL", "http://${wiremockContainer.host}:${wiremockContainer.port}/wet-clinic")
        }
    }
}