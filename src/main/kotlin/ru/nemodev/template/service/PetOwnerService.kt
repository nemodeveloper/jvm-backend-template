package ru.nemodev.template.service

import org.springframework.stereotype.Service
import ru.nemodev.platform.core.exception.error.ErrorCode
import ru.nemodev.platform.core.exception.logic.NotFoundLogicalException
import ru.nemodev.template.entity.PetOwnerEntity
import ru.nemodev.template.repository.PetOwnerRepository
import java.util.*
import kotlin.jvm.optionals.getOrNull

interface PetOwnerService {

    fun findById(id: UUID): PetOwnerEntity?

    fun getById(id: UUID): PetOwnerEntity

    fun findAllById(ids: Set<UUID>): List<PetOwnerEntity>
}

@Service
class PetOwnerServiceImpl(
    private val repository: PetOwnerRepository
) : PetOwnerService {

    override fun findById(id: UUID) = repository.findById(id).getOrNull()

    override fun getById(id: UUID): PetOwnerEntity {
        return findById(id) ?: throw NotFoundLogicalException(
                errorCode = ErrorCode.createNotFound("Не найден владелец питомца id = $id")
            )
    }

    override fun findAllById(ids: Set<UUID>): List<PetOwnerEntity> {
        return repository.findAllById(ids)
    }
}