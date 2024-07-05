package ru.nemodev.template.entity

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.nemodev.platform.core.db.annotation.StoreJson
import ru.nemodev.platform.core.db.entity.AbstractEntity
import java.time.LocalDateTime
import java.util.*

/**
 * Доменная/Бизнес сущность
 * 1 - Каждая Entity должна наследоваться от базовой AbstractEntity
 * 2 - Все поля хранятся в JSONB формате, исключение Id, DateTime, FK
 * 2.1 - Такой подход позволяет не менять структуру БД через DDL команды / легко поддерживать совместимость / хранить списки без таблиц связок и тд
 */
@Table("pets")
class PetEntity(
    id: UUID? = null, // Id сгенерируется автоматически перед вставкой в БД, реализацию смотри в core-db
    createdAt: LocalDateTime = LocalDateTime.now(), // Дата создания не обновляется при update запросах, реализацию смотри в core-db
    updatedAt: LocalDateTime = createdAt, // Дата обновления автоматически обновляется перед вставкой в БД, реализацию смотри в core-db

    @Column("pet_detail")
    val petDetail: PetDetail
) : AbstractEntity<UUID>(id, createdAt, updatedAt) {

    @Value("null")
    var owner: PetOwnerEntity? = null

    companion object {
        const val ENTITY_NAME = "pet"

        fun getFileName(fileName: String, extension: String) = "$fileName.$extension"
    }

    fun getFileName(extension: String) = getFileName(id.toString(), extension)
}

/**
 * Объект хранящийся в JSONB столбце
 * 1 - Обязательно должен быть помечен аннотацией StoreJson, реализацию смотри в core-db
 * 1.1 - Чтобы аннотация работала базовый пакет должен быть ru.nemodev.*
 */
@StoreJson
data class PetDetail(
    val clientId: UUID,
    val ownerId: UUID,
    var name: String,
    var type: PetType,
    var wetClinicRegistered: Boolean = false,
    val photos: MutableSet<String> = mutableSetOf()
)

enum class PetType {
    CAT,
    DOG,
    UNKNOWN
}
