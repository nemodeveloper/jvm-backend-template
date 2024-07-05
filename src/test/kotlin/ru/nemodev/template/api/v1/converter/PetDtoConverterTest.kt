package ru.nemodev.template.api.v1.converter

import org.junit.jupiter.api.Test
import ru.nemodev.template.api.v1.dto.PetDetailDtoRs
import ru.nemodev.template.entity.PetDetail
import ru.nemodev.template.entity.PetEntity
import ru.nemodev.template.entity.PetType
import java.util.*

class PetDtoConverterTest {

    companion object {
        private const val petDtoRsConvertFieldCount = 4
    }

    @Test
    fun convert() {
        val pet = generatePet()
        val petDtoRs = pet.toDetailDtoRs()

        assert(pet.id == petDtoRs.id)
        assert(pet.petDetail.name == petDtoRs.name)
        assert(pet.petDetail.type.name == petDtoRs.type.name)
    }

    @Test
    fun petDtoRsConvertFieldCount() {
        assert(PetDetailDtoRs::class.java.declaredFields.size == petDtoRsConvertFieldCount) {
            "Тест конвертера умеет проверять $petDtoRsConvertFieldCount поля, проверьте новые поля в PetDtoRs ${PetDetailDtoRs::class.java.declaredFields.map { it.name }} и добавить их в тест convert()"
        }
    }

    private fun generatePet() = PetEntity(
        id = UUID.randomUUID(),
        petDetail = PetDetail(
            clientId = UUID.randomUUID(),
            name = UUID.randomUUID().toString(),
            type = PetType.entries.random(),
            wetClinicRegistered = false,
            ownerId = UUID.randomUUID()
        )
    )
}