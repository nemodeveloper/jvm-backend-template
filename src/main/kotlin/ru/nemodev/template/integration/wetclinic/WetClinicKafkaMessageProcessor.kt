package ru.nemodev.template.integration.wetclinic

import io.github.springwolf.addons.generic_binding.annotation.AsyncGenericOperationBinding
import io.github.springwolf.core.asyncapi.annotations.AsyncListener
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import ru.nemodev.platform.core.integration.kafka.deserializer.DeserializeResult
import ru.nemodev.platform.core.logging.sl4j.Loggable
import ru.nemodev.template.integration.wetclinic.dto.PetRegistrationDtoRq
import ru.nemodev.template.service.PetService

@Component
class WetClinicKafkaMessageProcessor(
    private val petService: PetService
) {
    companion object : Loggable

    @AsyncGenericOperationBinding(
        type = "kafka",
        fields = [
            "bindingVersion=1.0.0",
            "groupId.type=string",
            "groupId.enum=wet-clinic.pets.registrations"
        ]
    )
    @AsyncListener(
        operation = AsyncOperation(
            channelName = "wet-clinic.pets.registrations.v1",
            description = "Обработка полученного сообщения регистрации питомца",
            payloadType = PetRegistrationDtoRq::class
        )
    )
    @KafkaListener(
        containerFactory = "wetClinicConcurrentKafkaListenerContainerFactory"
    )
    fun process(@Payload message: ConsumerRecord<String, DeserializeResult<PetRegistrationDtoRq>>) {
        when(val petData = message.value()) {
            is DeserializeResult.Success -> {
                val petRegistration = petData.data

                val pet = petService.findById(petRegistration.id)
                if (pet != null) {
                    pet.petDetail.wetClinicRegistered = true
                    petService.update(pet)
                    logInfo {
                        "Питомец id = ${pet.id} name = ${pet.petDetail.name} поставлен на учет в вет клинике =)"
                    }
                }
            }
            is DeserializeResult.Failed -> {

            }
        }
    }
}
