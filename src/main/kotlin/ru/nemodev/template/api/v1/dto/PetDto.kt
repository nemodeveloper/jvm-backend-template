package ru.nemodev.template.api.v1.dto

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.*

@Schema(description = "Питомец")
data class PetDetailDtoRs(
    @Schema(description = "Id питомца", example = "01899c74-7f8c-7012-bcfc-097dc8a5d3b7", minLength = 36, maxLength = 36)
    val id: UUID,
    @Schema(description = "Дата создания, формат ISO.DATE_TIME", example = "2023-08-16T12:49:09.672309", minLength = 26, maxLength = 26)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    val createdAt: LocalDateTime,
    @Schema(description = "Кличка питомца", example = "Мурка", minLength = 1, maxLength = 64)
    val name: String,
    @Schema(description = "Тип питомец", example = "CAT")
    val type: PetTypeDto
)

@Schema(description = "Тип питомца")
enum class PetTypeDto {
    DOG,
    CAT,
    UNKNOWN
}
