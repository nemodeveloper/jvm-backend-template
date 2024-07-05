package ru.nemodev.template.api.v1.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "Обновление питомца")
data class PetUpdateDtoRq(
    @Schema(description = "Кличка питомца", example = "Мурка", minLength = 1, maxLength = 64)
    @field:NotBlank(message = "{platform.NotEmpty.message}")
    @field:Size(message = "Размер поля должен быть от 1 до 64 символов", min = 1, max = 64)
    val name: String,
    @Schema(description = "Тип питомца", example = "CAT")
    val type: PetTypeDto
)