package ru.nemodev.template.integration.wetclinic

import io.github.springwolf.addons.generic_binding.annotation.AsyncGenericOperationBinding
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation
import io.github.springwolf.core.asyncapi.annotations.AsyncPublisher
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import ru.nemodev.platform.core.exception.critical.IntegrationCriticalException
import ru.nemodev.platform.core.exception.error.ErrorCode
import ru.nemodev.platform.core.exception.logic.IntegrationLogicException
import ru.nemodev.platform.core.integration.kafka.producer.SmartKafkaProducer
import ru.nemodev.template.config.integration.WetClinicProperties
import ru.nemodev.template.entity.PetEntity
import ru.nemodev.template.integration.wetclinic.converter.toPetRegistrationDtoRq
import ru.nemodev.template.integration.wetclinic.dto.PetRegistrationDtoRq
import ru.nemodev.template.integration.wetclinic.dto.PetRegistrationDtoRs
import java.util.*

/**
 * Интеграционный слой с сервисами
 * Задачи:
 * 1 - Выполнить запрос к внешнему(другому) сервису / Kafka и т.д
 * 2 - Методы могут принимать на вход бизнес сущности внутри преобразую их к внешним DTO или сразу же DTO
 * 3 - Возвращают методы всегда DTO
 * 4 - В случае ошибок методы должны выбрасывать *IntegrationException
 */
interface WetClinicIntegration {

    fun petRegistrationHttp(pet: PetEntity): PetRegistrationDtoRs

    fun petRegistrationKafka(pet: PetEntity)
}

@Component
class WetClinicIntegrationImpl(
    wetClinicProperties: WetClinicProperties,
    private val wetClinicRestClient: RestClient,
    private val wetClinicSmartProducer: SmartKafkaProducer<PetRegistrationDtoRq>
): WetClinicIntegration {

    companion object {
        private const val PET_REGISTRATION_PATH = "/v1/pet-registrations"
        private const val PET_REGISTRATION_DETAIL_PATH = "$PET_REGISTRATION_PATH/{id}"
    }

    private val serviceId = wetClinicProperties.integration.httpClient.serviceId

    override fun petRegistrationHttp(pet: PetEntity): PetRegistrationDtoRs {
        return wetClinicRestClient
            .post()
            .uri(PET_REGISTRATION_PATH)
            .body(pet.toPetRegistrationDtoRq())
            .retrieve()
            .onStatus(
                { it.is4xxClientError },
                { _, response ->
                    throw IntegrationLogicException(
                        serviceId = serviceId,
                        errorCode = ErrorCode.create("PET_REGISTRATION_ERROR", "Ошибка регистрации питомца в ветклинике"),
                        httpStatus = response.statusCode
                    )
                }
            )
            .onStatus(
                { it.is5xxServerError },
                { _, response ->
                    throw IntegrationCriticalException(
                        serviceId = serviceId,
                        errorCode = ErrorCode.create("PET_REGISTRATION_ERROR", "Ошибка регистрации питомца в ветклинике"),
                        httpStatus = response.statusCode
                    )
                }
            )
            .toEntity(PetRegistrationDtoRs::class.java)
            .body!!
    }

    override fun petRegistrationKafka(pet: PetEntity) {
        producePetRegistrationMessage(pet.id.toString(), pet.toPetRegistrationDtoRq())
    }

    @AsyncGenericOperationBinding(
        type = "kafka",
        fields = [
            "bindingVersion=1.0.0",
            "groupId.type=string",
            "groupId.enum=wet-clinic.pets.registrations"
        ]
    )
    @AsyncPublisher(
        operation = AsyncOperation(
            channelName = "wet-clinic.pets.registrations.v1",
            description = "Публикация сообщения регистрации питомца",
            payloadType = PetRegistrationDtoRq::class
        )
    )
    private fun producePetRegistrationMessage(
        key: String,
        @Payload value: PetRegistrationDtoRq
    ) = wetClinicSmartProducer.send(key, value)

    /**
     * Пример правильной передачи:
     * 1 - path-params
     * 2 - query-params
     * 3 - headers
     * В URI RestClient
     * Если передавать все параметры сразу же строкой на каждый запрос будет генерироваться уникальная метрика
     * Что не правильно и может положить сервис метрик
     * После написания запроса к сервису проверяйте что по нему генерируется 1 метрика в /actuator/prometheus
     * Название метрики http_client_requests_seconds_count
     * Метрика появится после вызова сервиса
     */
    private fun getRegistrationById(
        id: UUID,
        testHeader: String,
        testParam: String,
        optionalParam: String? = null
    ) {
        // Пример с path + query параметрами в URI
        wetClinicRestClient
            .get()
            .uri(PET_REGISTRATION_DETAIL_PATH) { uriBuilder ->
                uriBuilder.queryParam("testParam", "testValue") // Передача обязательного Query параметра
                optionalParam?.let { uriBuilder.queryParam("testParam", testParam) } // Передача опционального Query параметра
                uriBuilder.build(id)    // Передача path параметров, в данном случае id ресурса
            }
            .header("testHeader", testHeader)   // Передача заголовка
            .retrieve()
            .onStatus(
                { it.is4xxClientError },
                { _, response ->
                    throw IntegrationLogicException(
                        serviceId = serviceId,
                        errorCode = ErrorCode.create("PET_REGISTRATION_ERROR", "Ошибка регистрации питомца в ветклинике"),
                        httpStatus = response.statusCode
                    )
                }
            )
            .onStatus(
                { it.is5xxServerError },
                { _, response ->
                    throw IntegrationCriticalException(
                        serviceId = serviceId,
                        errorCode = ErrorCode.create("PET_REGISTRATION_ERROR", "Ошибка регистрации питомца в ветклинике"),
                        httpStatus = response.statusCode
                    )
                }
            )
            .toEntity(PetRegistrationDtoRs::class.java)
            .body!!

        // Пример только с path параметрами в URI
        wetClinicRestClient
            .get()
            .uri(PET_REGISTRATION_DETAIL_PATH, id)
            .header("testHeader", testHeader)   // Передача заголовка
            .retrieve()
            .onStatus(
                { it.is4xxClientError },
                { _, response ->
                    throw IntegrationLogicException(
                        serviceId = serviceId,
                        errorCode = ErrorCode.create("PET_REGISTRATION_ERROR", "Ошибка регистрации питомца в ветклинике"),
                        httpStatus = response.statusCode
                    )
                }
            )
            .onStatus(
                { it.is5xxServerError },
                { _, response ->
                    throw IntegrationCriticalException(
                        serviceId = serviceId,
                        errorCode = ErrorCode.create("PET_REGISTRATION_ERROR", "Ошибка регистрации питомца в ветклинике"),
                        httpStatus = response.statusCode
                    )
                }
            )
            .toEntity(String::class.java)
            .body!!
    }
}
