package ru.nemodev.template.config.integration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.nemodev.platform.core.integration.http.config.RestClientProperties
import ru.nemodev.platform.core.integration.http.factory.RestClientFactory
import ru.nemodev.platform.core.integration.kafka.config.KafkaIntegrationProperties
import ru.nemodev.platform.core.integration.kafka.consumer.KafkaMessageProcessor
import ru.nemodev.platform.core.integration.kafka.consumer.SmartKafkaConsumer
import ru.nemodev.platform.core.integration.kafka.factory.SmartKafkaFactory
import ru.nemodev.platform.core.integration.kafka.producer.SmartKafkaProducer
import ru.nemodev.template.integration.wetclinic.dto.PetRegistrationDtoRq

@ConfigurationProperties("wet-clinic")
class WetClinicProperties(
    val integration: WetClinicIntegration
) {
    data class WetClinicIntegration(
        val httpClient: RestClientProperties,
        val kafka: KafkaIntegrationProperties
    )
}

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(WetClinicProperties::class)
class WetClinicIntegrationConfig {

    companion object {
        private const val WET_CLINIC_PRODUCER_KEY = "wet-clinic.pets.registrations"
        private const val WET_CLINIC_CONSUMER_KEY = "wet-clinic.pets.registrations"
    }

    @Bean
    fun wetClinicRestClient(
        properties: WetClinicProperties,
        restClientFactory: RestClientFactory
    ) = restClientFactory.create(properties.integration.httpClient)

    @Bean
    fun wetClinicSmartProducer(
        smartKafkaFactory: SmartKafkaFactory,
        properties: WetClinicProperties
    ): SmartKafkaProducer<PetRegistrationDtoRq> =
        smartKafkaFactory.createProducer(WET_CLINIC_PRODUCER_KEY, properties.integration.kafka)

    @Bean
    fun wetClinicSmartConsumer(
        smartKafkaFactory: SmartKafkaFactory,
        properties: WetClinicProperties,
        wetClinicKafkaMessageProcessor: KafkaMessageProcessor<PetRegistrationDtoRq>
    ): SmartKafkaConsumer<PetRegistrationDtoRq> =
        smartKafkaFactory.createConsumer(
            WET_CLINIC_CONSUMER_KEY,
            properties.integration.kafka,
            wetClinicKafkaMessageProcessor,
            PetRegistrationDtoRq::class.java
        )
}