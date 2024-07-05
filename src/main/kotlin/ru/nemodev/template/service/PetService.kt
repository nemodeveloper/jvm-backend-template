package ru.nemodev.template.service

import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import ru.nemodev.platform.core.api.domen.file.FileData
import ru.nemodev.platform.core.async.executor.IOCoroutineExecutor
import ru.nemodev.platform.core.exception.error.ErrorCode
import ru.nemodev.platform.core.exception.error.ErrorField
import ru.nemodev.platform.core.exception.logic.ForbiddenLogicalException
import ru.nemodev.platform.core.exception.logic.NotFoundLogicalException
import ru.nemodev.platform.core.exception.logic.ValidationLogicException
import ru.nemodev.platform.core.extensions.getFileExtension
import ru.nemodev.platform.core.extensions.getMediaType
import ru.nemodev.platform.core.integration.s3.minio.client.MinioS3Client
import ru.nemodev.platform.core.logging.sl4j.Loggable
import ru.nemodev.template.api.v1.dto.PetCreateDtoRq
import ru.nemodev.template.api.v1.dto.PetUpdateDtoRq
import ru.nemodev.template.entity.PetDetail
import ru.nemodev.template.entity.PetEntity
import ru.nemodev.template.entity.PetType
import ru.nemodev.template.integration.wetclinic.WetClinicIntegration
import ru.nemodev.template.repository.PetCustomRepository
import ru.nemodev.template.repository.PetRepository
import java.util.*
import kotlin.jvm.optionals.getOrNull

/**
 * Бизнес слой сервиса
 * Задачи:
 * 1 - Выполнение непосредственной бизнес логики
 * 2 - Методы на вход могут принимать DTO объекты, возвращать DTO запрещено, всегда возвращаем Entity или бизнес(доменную) модель
 * 3 - Старайтесь чтобы ваш метод выглядел как оркестратор который по цепочке вызывает другие сервисы/билдеры/репозитории
 * 4 - Старайтесь избегать жесткой связанности между разными бизнес сервисами, соблюдайте гранулярность операций
 * 5 - Делайте вспомогательные классы для решения задач билдеры/конвертеры/утилиты/расширения и т.д
 * 6 - Если размер имплементации начинает превышать 300 строк возможно ваш сервис стоит разбить на несколько специализированных
 * 7 - В случае ошибок методы должны выбрасывать *Logic/CriticalException
 * 8 - Сервис может запустить событие трассировки как и Processor слой
 */
interface PetService {

    fun create(clientId: UUID, request: PetCreateDtoRq): PetEntity

    fun findAll(
        clientId: UUID,
        pageable: Pageable
    ): List<PetEntity>

    fun findById(id: UUID): PetEntity?

    fun findById(id: UUID, clientId: UUID): PetEntity

    fun update(pet: PetEntity): PetEntity

    fun deleteById(id: UUID, clientId: UUID)

    fun updateById(id: UUID, clientId: UUID, request: PetUpdateDtoRq)

    fun uploadPhotoById(id: UUID, clientId: UUID, filePart: MultipartFile)

    fun downloadPhoto(id: UUID, clientId: UUID): FileData
}

@Service
class PetServiceImpl(
    private val repository: PetRepository,
    private val customRepository: PetCustomRepository,
    private val wetClinicIntegration: WetClinicIntegration,
    private val petOwnerService: PetOwnerService,
    private val petMetricService: PetMetricService,
    private val coroutineExecutor: IOCoroutineExecutor,
    private val minioS3Client: MinioS3Client
) : PetService {

    companion object : Loggable { // интерфейс Loggable автоматически создает logger по имени класса и добавляет методы log*
        // Для демонстрационных целей
        private val STATIC_PET_OWNER = UUID.fromString("1ab6229a-4e7b-4ac0-a7d0-f40d60ce1e59")
    }

    override fun create(clientId: UUID, request: PetCreateDtoRq): PetEntity {
        if (request.name == "Мурка") {
            // Все исключения делятся на 2 типа LogicException и CriticalException
            // Наследники LogicException представляют собой 4** http статус коды, обычно такую ошибку можно исправить инициатору
            // Наследники CriticalException представляют собой 5** http статус коды, обычно такую ошибку не исправить инициатору
            // Обработка исключений реализована в ApiExceptionHandler
            throw ValidationLogicException(
                errorCode = ErrorCode.createValidation("Слишком много Мурок"),
                errorFields = listOf(
                    ErrorField.create(
                        key = "name",   // Название поля которое привело к ошибке, например json-path в запросе
                        code = "PET_NAME_TO_MANY",  // Код ошибки
                        description = "Уже достаточно Мурок выберите другое имя питомца" // Человеко-читаемая ошибка
                    )
                )
            )
        }

        return repository.save(
            PetEntity(
                petDetail = PetDetail(
                    clientId = clientId,
                    name = request.name,
                    type = PetType.valueOf(request.type.name),
                    ownerId = STATIC_PET_OWNER
                )
            )
        ).also {
            // Кастомная тест метрика создания питомца
            petMetricService.created(it)

            // Отправка запросов будет выполнена асинхронно в фоне не блокируя текущий метод
            // Важно понимать что если сервис упадет то вызов внешнего сервиса/kafka так же может не успеть выполниться
            // Имейте это ввиду когда пытаетесь что-то выполнить в фоне
            coroutineExecutor.launch { wetClinicIntegration.petRegistrationHttp(it) }
            coroutineExecutor.launch { wetClinicIntegration.petRegistrationKafka(it) }

            // Отправка запросов будет выполнена асинхронно и параллельно, метод create будет дождаться исполнения
            // Важно понимать что это не решит проблему, описанную выше если ваш сервис упадет или сторонний сервис/kafka будут не доступны
            // Но позволит выполнить 2 запроса одновременно, а не последовательно

            runBlocking {
                listOf(
                    coroutineExecutor.async { wetClinicIntegration.petRegistrationHttp(it) },
                    coroutineExecutor.async { wetClinicIntegration.petRegistrationKafka(it) }
                ).awaitAll()
            }

            // Обычные синхронные запросы с ожиданием ответа
            wetClinicIntegration.petRegistrationHttp(it)
            wetClinicIntegration.petRegistrationKafka(it)
        }
    }

    override fun findAll(
        clientId: UUID,
        pageable: Pageable
    ): List<PetEntity> {
        // Получаем список питомцев через кастомный репозиторий с поддержкой pageable
        // Например если у вас сложный динамический запрос с условиями и сортировкой
        var pets = customRepository.findAllByClientId(clientId, pageable)

        // Иначе используем простые spring-data запросы через repository.findAllByClientId
        pets = repository.findAllByClientId(
            clientId = clientId.toString(),
            offset = pageable.offset,
            limit = pageable.pageSize
        )

        // Ищем владельцев
        val petOwnerMap = petOwnerService
            .findAllById(pets.map { it.petDetail.ownerId }.toSet())
            .associateBy { it.id }

        // Обогащаем сущность питомца связанной сущностью владелец если это необходимо
        pets.forEach {
            it.owner = petOwnerMap[it.petDetail.ownerId]
        }

        return pets
    }

    override fun findById(id: UUID): PetEntity? {
        return repository.findById(id).getOrNull()?.also {
            it.owner = petOwnerService.getById(it.petDetail.ownerId)
        }
    }

    override fun findById(id: UUID, clientId: UUID): PetEntity {
        val pet = findById(id)
            ?: throw NotFoundLogicalException(
                errorCode = ErrorCode.createNotFound("Питомец не найден")
            )

        if (pet.petDetail.clientId != clientId) {
            throw ForbiddenLogicalException(
                errorCode = ErrorCode.createForbidden("Просмотр питомца не доступен")
            )
        }

        return pet
    }

    override fun update(pet: PetEntity): PetEntity = repository.save(pet)

    override fun deleteById(id: UUID, clientId: UUID) {
        findById(id = id, clientId = clientId)
        repository.deleteById(id)
    }

    override fun updateById(id: UUID, clientId: UUID, request: PetUpdateDtoRq) {
        val pet = findById(id = id, clientId = clientId)

        // Базовую проверку полей из PetUpdateDtoRq не делаем т.к поля уже прошли валидацию NotBlank/Size
        pet.petDetail.name = request.name
        pet.petDetail.type = PetType.valueOf(request.type.name)

        repository.save(pet)
    }

    override fun uploadPhotoById(id: UUID, clientId: UUID, filePart: MultipartFile) {
        val pet = findById(id, clientId)
        val petFileName = filePart.originalFilename!!
        pet.petDetail.photos.add(petFileName)

        repository.save(pet)
        minioS3Client.upload(
            fileName = "$id.${petFileName.getFileExtension()}",
            file = filePart.bytes
        )
    }

    override fun downloadPhoto(id: UUID, clientId: UUID): FileData {
        val pet = findById(id, clientId)

        if (pet.petDetail.photos.isEmpty()) {
            throw NotFoundLogicalException(
                errorCode = ErrorCode.createNotFound("Не найдено ни одного фото питомца")
            )
        }

        val fileExtension = pet.petDetail.photos.first().getFileExtension()
        val fileName = pet.getFileName(fileExtension)
        try {
            val inputStream = minioS3Client.download(fileName = fileName)

            return FileData(
                fileName,
                fileExtension,
                fileExtension.getMediaType(),
                inputStream.readAllBytes()
            )
        } catch (e: Exception) {
            throw NotFoundLogicalException(
                errorCode = ErrorCode.createNotFound("Ошибка скачивания фото питомца"),
                cause = e,
                message = e.message
            )
        }
    }
}