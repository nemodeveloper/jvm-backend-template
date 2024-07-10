package ru.nemodev.template.config.integration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import ru.nemodev.platform.core.integration.http.config.RestClientProperties
import ru.nemodev.platform.core.integration.http.factory.RestClientFactory
import ru.nemodev.platform.core.integration.kafka.config.KafkaIntegrationProperties
import ru.nemodev.platform.core.integration.kafka.deserializer.DeserializeResult
import ru.nemodev.platform.core.integration.kafka.factory.PlatformKafkaFactory
import ru.nemodev.platform.core.integration.kafka.producer.PlatformKafkaProducer
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
    fun wetClinicDefaultKafkaProducerFactory(
        properties: WetClinicProperties,
        platformKafkaFactory: PlatformKafkaFactory,
    ): DefaultKafkaProducerFactory<String, PetRegistrationDtoRq> =
        platformKafkaFactory.createDefaultKafkaProducerFactory(
            WET_CLINIC_PRODUCER_KEY,
            properties.integration.kafka
        )

    @Bean
    fun wetClinicKafkaTemplate(
        properties: WetClinicProperties,
        platformKafkaFactory: PlatformKafkaFactory,
        wetClinicDefaultKafkaProducerFactory: DefaultKafkaProducerFactory<String, PetRegistrationDtoRq>
    ): KafkaTemplate<String, PetRegistrationDtoRq> =
        platformKafkaFactory.createKafkaTemplate(
            WET_CLINIC_PRODUCER_KEY,
            properties.integration.kafka,
            wetClinicDefaultKafkaProducerFactory
        )

    @Bean
    fun wetClinicPlatformKafkaProducer(
        platformKafkaFactory: PlatformKafkaFactory,
        properties: WetClinicProperties,
        wetClinicKafkaTemplate: KafkaTemplate<String, PetRegistrationDtoRq>
    ): PlatformKafkaProducer<PetRegistrationDtoRq> =
        platformKafkaFactory.createProducer(
            WET_CLINIC_PRODUCER_KEY,
            properties.integration.kafka,
            wetClinicKafkaTemplate
        )

    @Bean
    fun wetClinicDefaultKafkaConsumerFactory(
        properties: WetClinicProperties,
        platformKafkaFactory: PlatformKafkaFactory
    ): DefaultKafkaConsumerFactory<String, DeserializeResult<PetRegistrationDtoRq>> =
        platformKafkaFactory.createDefaultKafkaConsumerFactory(
            WET_CLINIC_CONSUMER_KEY,
            properties.integration.kafka,
            PetRegistrationDtoRq::class.java
        )

    @Bean
    fun wetClinicConcurrentKafkaListenerContainerFactory(
        properties: WetClinicProperties,
        platformKafkaFactory: PlatformKafkaFactory,
        wetClinicDefaultKafkaConsumerFactory: DefaultKafkaConsumerFactory<String, DeserializeResult<PetRegistrationDtoRq>>
    ): ConcurrentKafkaListenerContainerFactory<String, DeserializeResult<PetRegistrationDtoRq>> =
        platformKafkaFactory.createConcurrentKafkaListenerContainerFactory(
            WET_CLINIC_CONSUMER_KEY,
            properties.integration.kafka,
            wetClinicDefaultKafkaConsumerFactory
        )
}