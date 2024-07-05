package ru.nemodev.template.integration.wetclinic.converter

import ru.nemodev.template.entity.PetEntity
import ru.nemodev.template.integration.wetclinic.dto.PetRegistrationDtoRq
import ru.nemodev.template.integration.wetclinic.dto.WetClinicPetTypeDto

fun PetEntity.toPetRegistrationDtoRq() =
    PetRegistrationDtoRq(
        id = id,
        name = petDetail.name,
        type = WetClinicPetTypeDto.valueOf(petDetail.type.name)
    )