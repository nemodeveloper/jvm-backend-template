package ru.nemodev.template.api.v1.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import ru.nemodev.platform.core.api.dto.error.ErrorDtoRs
import ru.nemodev.platform.core.api.dto.paging.PageDtoRs
import ru.nemodev.platform.core.api.headers.ApiHeaderNames
import ru.nemodev.template.api.v1.dto.PetCreateDtoRq
import ru.nemodev.template.api.v1.dto.PetCreateDtoRs
import ru.nemodev.template.api.v1.dto.PetDetailDtoRs
import ru.nemodev.template.api.v1.dto.PetUpdateDtoRq
import ru.nemodev.template.api.v1.processor.PetProcessor
import java.util.*

/**
 * Слой API(Controller)
 * Задачи:
 * 1 - Описать API контракт в формате OpenAPI v3
 * 2 - Принять запрос с заданными параметрами
 * 3 - Произвести первоначальную валидацию структуры и значений полей запроса
 * 4 - Передать управление слою Processor
 * ВНИМАНИЕ - Запрещено реализовывать любую бизнес логику и цепочки вызовов методов Processor
 */
@RestController
@RequestMapping("/v1/pets", produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(name = "Питомцы", description = "API питомцев")
@SecurityRequirement(name = "apiKeyAuth")
@Validated
class PetController(
    private val processor: PetProcessor
) {

    @Operation(
        summary = "Список, отсортированный по дате создания desc",
        responses = [
            ApiResponse(responseCode = "200", description = "Успешный ответ"),
            ApiResponse(responseCode = "400", description = "Не правильный формат запроса",
                content = [Content(
                    schema = Schema(implementation = ErrorDtoRs::class),
                    examples = [
                        ExampleObject(name = "400", value = "error400")
                    ]
                )]
            ),
            ApiResponse(responseCode = "401", description = "Неавторизованный запрос"),
            ApiResponse(responseCode = "422", description = "Ошибка валидации полей запроса",
                content = [Content(
                    schema = Schema(implementation = ErrorDtoRs::class),
                    examples = [
                        ExampleObject(name = "422", value = "error422")
                    ]
                )]
            ),
            ApiResponse(responseCode = "500", description = "Ошибка обработки запроса",
                content = [Content(
                    schema = Schema(implementation = ErrorDtoRs::class),
                    examples = [
                        ExampleObject(name = "500", value = "error500")
                    ]
                )]
            )
        ]
    )
    @GetMapping
    fun findAll(
        @Parameter(description = "api-key", example = "55298707-117c-44a5-8434-162fb9d2e4c5", required = true)
        @RequestHeader(ApiHeaderNames.API_KEY)
        apiKey: UUID,

        @Parameter(description = "Id клиента", example = "4258c14c-d07e-41e6-be77-c1831ef227d8", required = true)
        @RequestHeader(ApiHeaderNames.USER_ID)
        clientId: UUID,

        @Parameter(description = "Номер страницы результата, минимальное значение 0, значение по умолчанию 0", example = "0", required = false)
        @RequestParam(name = "pageNumber", required = false)
        @Valid
        @Min(0, message = "Минимальное значение 0")
        pageNumber: Int? = null,

        @Parameter(description = "Размер страницы результата, минимальное значение 1, максимальное значение 100, значение по умолчанию 25", example = "25", required = false)
        @RequestParam(name = "pageSize", required = false)
        @Valid
        @Min(1, message = "Минимальное значение 1")
        @Max(100, message = "Максимальное значение 100")
        pageSize: Int? = null
    ): PageDtoRs<PetDetailDtoRs> {
        return processor.findAll(
            clientId = clientId,
            pageable = PageRequest.of(
                pageNumber ?: 0,
                pageSize ?: 25,
                Sort.by(Sort.Order.desc("created_at"))
            )
        )
    }

    @Operation(
        summary = "Создать",
        responses = [
            ApiResponse(responseCode = "201", description = "Успешный ответ",
                content = [Content(schema = Schema(implementation = PetCreateDtoRq::class))]
            ),
            ApiResponse(responseCode = "400", description = "Не правильный формат запроса",
                content = [Content(
                    schema = Schema(implementation = ErrorDtoRs::class),
                    examples = [ExampleObject(value = "@swagger/example/pets_error_400_rs.json")]
                )]
            ),
            ApiResponse(responseCode = "401", description = "Неавторизованный запрос"),
            ApiResponse(responseCode = "422", description = "Ошибка валидации полей запроса",
                content = [Content(
                    schema = Schema(implementation = ErrorDtoRs::class),
                    examples = [ExampleObject(value = "@swagger/example/pets_error_422_rs.json")]
                )]
            ),
            ApiResponse(responseCode = "500", description = "Ошибка обработки запроса",
                content = [Content(
                    schema = Schema(implementation = ErrorDtoRs::class),
                    examples = [ExampleObject(value = "@swagger/example/pets_error_500_rs.json")]
                )]
            )
        ]
    )
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @Parameter(description = "api-key", example = "55298707-117c-44a5-8434-162fb9d2e4c5", required = true)
        @RequestHeader(ApiHeaderNames.API_KEY)
        apiKey: UUID,

        @Parameter(description = "Id клиента", example = "4258c14c-d07e-41e6-be77-c1831ef227d8", required = true)
        @RequestHeader(ApiHeaderNames.USER_ID)
        clientId: UUID,

        @RequestBody
        @Valid
        petCreateDtoRq: PetCreateDtoRq
    ): PetCreateDtoRs {
        return processor.create(clientId, petCreateDtoRq)
    }

    @Operation(
        summary = "Детали",
        responses = [
            ApiResponse(responseCode = "200", description = "Успешный ответ",
                content = [Content(schema = Schema(implementation = PetDetailDtoRs::class))]
            ),
            ApiResponse(responseCode = "400", description = "Не правильный формат запроса",
                content = [Content(
                    schema = Schema(implementation = ErrorDtoRs::class),
                    examples = [ExampleObject(name = "1", value = "@swagger/example/pets_error_400_rs.json")]
                )]
            ),
            ApiResponse(responseCode = "401", description = "Неавторизованный запрос"),
            ApiResponse(responseCode = "403", description = "Нет прав",
                content = [Content(
                    schema = Schema(implementation = ErrorDtoRs::class),
                    examples = [ExampleObject(name = "2", value = "@swagger/example/pets_error_403_rs.json")]
                )]
            ),
            ApiResponse(responseCode = "404", description = "Не найден",
                content = [Content(
                    schema = Schema(implementation = ErrorDtoRs::class),
                    examples = [ExampleObject(name = "3", value = "@swagger/example/pets_error_404_rs.json")]
                )]
            ),
            ApiResponse(responseCode = "500", description = "Ошибка обработки запроса",
                content = [Content(
                    schema = Schema(implementation = ErrorDtoRs::class),
                    examples = [ExampleObject(name = "4", value = "@swagger/example/pets_error_500_rs.json")]
                )]
            )
        ]
    )
    @GetMapping("/{id}")
    fun findById(
        @Parameter(description = "api-key", example = "55298707-117c-44a5-8434-162fb9d2e4c5", required = true)
        @RequestHeader(ApiHeaderNames.API_KEY)
        apiKey: UUID,

        @Parameter(description = "Id клиента", example = "4258c14c-d07e-41e6-be77-c1831ef227d8", required = true)
        @RequestHeader(ApiHeaderNames.USER_ID)
        clientId: UUID,

        @Parameter(description = "Id питомца", example = "4258c14c-d07e-41e6-be77-c1831ef227d8", required = true)
        @PathVariable("id")
        id: UUID
    ): PetDetailDtoRs {
        return processor.findById(id, clientId)
    }

    @Operation(
        summary = "Загрузка фото питомца",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = [Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)]
        ),
        responses = [
            ApiResponse(responseCode = "204", description = "Успешный ответ"),
            ApiResponse(responseCode = "400", description = "Не правильный формат запроса",
                content = [Content(
                    schema = Schema(implementation = ErrorDtoRs::class),
                    examples = [ExampleObject(value = "@swagger/example/pets_error_400_rs.json")]
                )]
            ),
            ApiResponse(responseCode = "401", description = "Неавторизованный запрос"),
            ApiResponse(responseCode = "403", description = "Нет прав",
                content = [Content(
                    schema = Schema(implementation = ErrorDtoRs::class),
                    examples = [ExampleObject(value = "@swagger/example/pets_error_403_rs.json")]
                )]
            ),
            ApiResponse(responseCode = "404", description = "Не найден",
                content = [Content(
                    schema = Schema(implementation = ErrorDtoRs::class),
                    examples = [ExampleObject(value = "@swagger/example/pets_error_404_rs.json")]
                )]
            ),
            ApiResponse(responseCode = "422", description = "Ошибка валидации полей запроса",
                content = [Content(
                    schema = Schema(implementation = ErrorDtoRs::class),
                    examples = [ExampleObject(value = "@swagger/example/pets_error_422_rs.json")]
                )]
            ),
            ApiResponse(responseCode = "500", description = "Ошибка обработки запроса",
                content = [Content(
                    schema = Schema(implementation = ErrorDtoRs::class),
                    examples = [ExampleObject(value = "@swagger/example/pets_error_500_rs.json")]
                )]
            )
        ]
    )
    @PutMapping("/{id}/photo", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun uploadPhotoById(
        @Parameter(description = "api-key", example = "55298707-117c-44a5-8434-162fb9d2e4c5", required = true)
        @RequestHeader(ApiHeaderNames.API_KEY)
        apiKey: UUID,

        @Parameter(description = "Id клиента", example = "4258c14c-d07e-41e6-be77-c1831ef227d8", required = true)
        @RequestHeader(ApiHeaderNames.USER_ID)
        clientId: UUID,

        @Parameter(description = "Id питомца", example = "01899c74-7f8c-7012-bcfc-097dc8a5d3b7", required = true)
        @PathVariable("id")
        id: UUID,

        @RequestPart("file")
        filePart: MultipartFile
    ) {
        processor.uploadPhotoById(id, clientId, filePart)
    }

    @Operation(
        summary = "Выгрузить фото питомца",
        responses = [
            ApiResponse(responseCode = "200", description = "Успешный ответ"),
            ApiResponse(
                responseCode = "400", description = "Не правильный формат запроса",
                content = [Content(schema = Schema(implementation = ErrorDtoRs::class))]
            ),
            ApiResponse(
                responseCode = "401", description = "Неавторизованный запрос",
                content = [Content(schema = Schema(implementation = ErrorDtoRs::class))]
            ),
            ApiResponse(responseCode = "403", description = "Нет прав",
                content = [Content(
                    schema = Schema(implementation = ErrorDtoRs::class),
                    examples = [ExampleObject(value = "@swagger/example/pets_error_403_rs.json")]
                )]
            ),
            ApiResponse(
                responseCode = "404", description = "Ресурс не найден",
                content = [Content(schema = Schema(implementation = ErrorDtoRs::class))]
            ),
            ApiResponse(responseCode = "422", description = "Ошибка валидации полей запроса",
                content = [Content(
                    schema = Schema(implementation = ErrorDtoRs::class),
                    examples = [ExampleObject(value = "@swagger/example/pets_error_422_rs.json")]
                )]
            ),
            ApiResponse(
                responseCode = "500", description = "Ошибка обработки запроса",
                content = [Content(schema = Schema(implementation = ErrorDtoRs::class))]
            )
        ]
    )
    @GetMapping("/{id}/photo")
    fun downloadPhoto(
        @Parameter(description = "api-key", example = "55298707-117c-44a5-8434-162fb9d2e4c5", required = true)
        @RequestHeader(ApiHeaderNames.API_KEY)
        apiKey: UUID,

        @Parameter(description = "Id клиента", example = "4258c14c-d07e-41e6-be77-c1831ef227d8", required = true)
        @RequestHeader(ApiHeaderNames.USER_ID)
        clientId: UUID,

        @Parameter(description = "Id питомца", example = "01899c74-7f8c-7012-bcfc-097dc8a5d3b7", required = true)
        @PathVariable("id")
        id: UUID,
    ): ResponseEntity<ByteArray> {
        return processor.downloadPhoto(id, clientId)
    }

    @Operation(
        summary = "Обновление",
        responses = [
            ApiResponse(responseCode = "204", description = "Успешный ответ",
                content = [Content(schema = Schema(implementation = PetUpdateDtoRq::class))]
            ),
            ApiResponse(responseCode = "400", description = "Не правильный формат запроса",
                content = [Content(
                    schema = Schema(implementation = ErrorDtoRs::class),
                    examples = [ExampleObject(value = "@swagger/example/pets_error_400_rs.json")]
                )]
            ),
            ApiResponse(responseCode = "401", description = "Неавторизованный запрос"),
            ApiResponse(responseCode = "403", description = "Нет прав",
                content = [Content(
                    schema = Schema(implementation = ErrorDtoRs::class),
                    examples = [ExampleObject(value = "@swagger/example/pets_error_403_rs.json")]
                )]
            ),
            ApiResponse(responseCode = "404", description = "Не найден",
                content = [Content(
                    schema = Schema(implementation = ErrorDtoRs::class),
                    examples = [ExampleObject(value = "@swagger/example/pets_error_404_rs.json")]
                )]
            ),
            ApiResponse(responseCode = "422", description = "Ошибка валидации полей запроса",
                content = [Content(
                    schema = Schema(implementation = ErrorDtoRs::class),
                    examples = [ExampleObject(value = "@swagger/example/pets_error_422_rs.json")]
                )]
            ),
            ApiResponse(responseCode = "500", description = "Ошибка обработки запроса",
                content = [Content(
                    schema = Schema(implementation = ErrorDtoRs::class),
                    examples = [ExampleObject(value = "@swagger/example/pets_error_500_rs.json")]
                )]
            )
        ]
    )
    @PutMapping("/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun updateById(
        @Parameter(description = "api-key", example = "55298707-117c-44a5-8434-162fb9d2e4c5", required = true)
        @RequestHeader(ApiHeaderNames.API_KEY)
        apiKey: UUID,

        @Parameter(description = "Id клиента", example = "4258c14c-d07e-41e6-be77-c1831ef227d8", required = true)
        @RequestHeader(ApiHeaderNames.USER_ID)
        clientId: UUID,

        @Parameter(description = "Id питомца", example = "01899c74-7f8c-7012-bcfc-097dc8a5d3b7", required = true)
        @PathVariable("id")
        id: UUID,

        @RequestBody
        @Valid
        petUpdateDtoRq: PetUpdateDtoRq
    ) {
        processor.updateById(id = id, clientId = clientId, petUpdateDtoRq)
    }

    @Operation(
        summary = "Удаление",
        responses = [
            ApiResponse(responseCode = "204", description = "Успешный ответ"),
            ApiResponse(responseCode = "401", description = "Неавторизованный запрос"),
            ApiResponse(responseCode = "403", description = "Нет прав",
                content = [Content(
                    schema = Schema(implementation = ErrorDtoRs::class),
                    examples = [ExampleObject(value = "@swagger/example/pets_error_403_rs.json")]
                )]
            ),
            ApiResponse(responseCode = "404", description = "Не найден",
                content = [Content(
                    schema = Schema(implementation = ErrorDtoRs::class),
                    examples = [ExampleObject(value = "@swagger/example/pets_error_404_rs.json")]
                )]
            ),
            ApiResponse(responseCode = "500", description = "Ошибка обработки запроса",
                content = [Content(
                    schema = Schema(implementation = ErrorDtoRs::class),
                    examples = [ExampleObject(value = "@swagger/example/pets_error_500_rs.json")]
                )]
            )
        ]
    )
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteById(
        @Parameter(description = "api-key", example = "55298707-117c-44a5-8434-162fb9d2e4c5", required = true)
        @RequestHeader(ApiHeaderNames.API_KEY)
        apiKey: UUID,

        @Parameter(description = "Id клиента", example = "4258c14c-d07e-41e6-be77-c1831ef227d8", required = true)
        @RequestHeader(ApiHeaderNames.USER_ID)
        clientId: UUID,

        @Parameter(description = "Id питомца", example = "01899c74-7f8c-7012-bcfc-097dc8a5d3b7", required = true)
        @PathVariable("id")
        id: UUID
    ) {
        processor.deleteById(id = id, clientId = clientId)
    }
}
