package ru.nemodev.template.integration.wetclinic.dto

import java.util.*

data class PetRegistrationDtoRq(
    val id: UUID,
    val name: String,
    val type: WetClinicPetTypeDto
)

data class PetRegistrationDtoRs(
    val id: String,
    val description: String
)

enum class WetClinicPetTypeDto {
    CAT,
    UNKNOWN
}

