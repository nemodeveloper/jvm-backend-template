package ru.nemodev.template.api.v1.processor

import io.micrometer.observation.ObservationRegistry
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import ru.nemodev.platform.core.api.dto.paging.PageDtoRs
import ru.nemodev.platform.core.tracing.extensions.createNotStarted
import ru.nemodev.template.api.v1.converter.toDetailDtoRs
import ru.nemodev.template.api.v1.dto.PetCreateDtoRq
import ru.nemodev.template.api.v1.dto.PetCreateDtoRs
import ru.nemodev.template.api.v1.dto.PetDetailDtoRs
import ru.nemodev.template.api.v1.dto.PetUpdateDtoRq
import ru.nemodev.template.service.PetService
import java.util.*
import java.util.function.Supplier

/**
 * Слой процессора нужен для связи API логики(Controller) с бизнес логикой(Service)
 * Задачи слоя:
 * 1 - Принять параметры из api, возможно с небольшим преобразованием типов
 * 2 - Запустить событие трассировки
 * 3 - Вызвать Service метод
 * 4 - Преобразовать Entity(доменную) модель в API DTO, если преобразование сложное и объемное выносим его в Converter
 * ВНИМАНИЕ - Запрещено реализовывать любую бизнес логику и цепочки вызовов методов Service
 */
interface PetProcessor {

    fun create(clientId: UUID, request: PetCreateDtoRq): PetCreateDtoRs

    fun findAll(
        clientId: UUID,
        pageable: Pageable
    ): PageDtoRs<PetDetailDtoRs>

    fun findById(id: UUID, clientId: UUID): PetDetailDtoRs

    fun uploadPhotoById(id: UUID, clientId: UUID, filePart: MultipartFile)

    fun downloadPhoto(id: UUID, clientId: UUID): ResponseEntity<ByteArray>

    fun deleteById(id: UUID, clientId: UUID)

    fun updateById(id: UUID, clientId: UUID, request: PetUpdateDtoRq)
}

@Component
class PetProcessorImpl(
    private val observationRegistry: ObservationRegistry,
    private val service: PetService,
) : PetProcessor {

    override fun create(clientId: UUID, request: PetCreateDtoRq): PetCreateDtoRs {
        // Пример события трассировки
        // Трассировать нужно только значимые события которые влияют на время исполнения или они критически важные для наблюдения
        // Трассировка http/db/kafka/web-client - работает из коробки
        return observationRegistry
            .createNotStarted("pet creation") // при событиях трассировки автоматически создаются одноименные метрики например pet_creation
            // крайне важно понимать разницу между lowCardinalityKeyValue и highCardinalityKeyValue
            .lowCardinalityKeyValue("pet.type", request.type.name) // для добавления значений которые поддаются исчислению и попадут в теги метрики
//            .highCardinalityKeyValue("clientId", clientId.toString()) // для добавления значений которые постоянно меняются и не попадут в теги метрики
            .observe(Supplier {
                PetCreateDtoRs(
                    id = service.create(
                        clientId = clientId,
                        request = request
                    ).id
                )
            })!!
    }

    override fun findAll(
        clientId: UUID,
        pageable: Pageable
    ): PageDtoRs<PetDetailDtoRs> {
        val pets = service.findAll(
            clientId = clientId,
            pageable = pageable
        )

        return PageDtoRs(
            items = pets.map {
                it.toDetailDtoRs()
            },
            pageNumber = pageable.pageNumber,
            hasMore = pets.size >= pageable.pageSize
        )
    }

    override fun findById(id: UUID, clientId: UUID): PetDetailDtoRs {
        return service
            .findById(id, clientId)
            .toDetailDtoRs()
    }

    override fun uploadPhotoById(id: UUID, clientId: UUID, filePart: MultipartFile) {
        service.uploadPhotoById(id, clientId, filePart)
    }

    override fun downloadPhoto(id: UUID, clientId: UUID): ResponseEntity<ByteArray> {
        return service.downloadPhoto(id, clientId).toResponseEntity()
    }

    override fun deleteById(id: UUID, clientId: UUID) {
        service.deleteById(id = id, clientId = clientId)
    }

    override fun updateById(id: UUID, clientId: UUID, request: PetUpdateDtoRq) {
        service.updateById(id = id, clientId = clientId, request)
    }
}