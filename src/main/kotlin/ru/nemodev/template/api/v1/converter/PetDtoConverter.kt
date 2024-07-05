package ru.nemodev.template.api.v1.converter

import ru.nemodev.template.api.v1.dto.PetDetailDtoRs
import ru.nemodev.template.api.v1.dto.PetTypeDto
import ru.nemodev.template.entity.PetEntity

fun PetEntity.toDetailDtoRs() =
    PetDetailDtoRs(
        id = id,
        createdAt = createdAt,
        name = petDetail.name,
        type = PetTypeDto.valueOf(petDetail.type.name)
    )