package ru.nemodev.template.api.v1.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.*

@Schema(description = "Создание питомца")
data class PetCreateDtoRq(
    @Schema(description = "Кличка питомца", example = "Мурка", minLength = 1, maxLength = 64)
    @field:NotBlank(message = "{platform.NotEmpty.message}")
    @field:Size(message = "Размер поля должен быть от 1 до 64 символов", min = 1, max = 64)
    val name: String,
    @Schema(description = "Тип питомца", example = "CAT")
    val type: PetTypeDto
)

@Schema(description = "Ответ создания питомца")
data class PetCreateDtoRs(
    @Schema(description = "Id питомца", example = "01899c74-7f8c-7012-bcfc-097dc8a5d3b7", minLength = 36, maxLength = 36)
    val id: UUID
)