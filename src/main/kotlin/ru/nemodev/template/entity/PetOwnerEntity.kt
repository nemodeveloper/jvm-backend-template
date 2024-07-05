package ru.nemodev.template.entity

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.nemodev.platform.core.db.annotation.StoreJson
import ru.nemodev.platform.core.db.entity.AbstractEntity
import java.time.LocalDateTime
import java.util.*

@Table("pet_owners")
class PetOwnerEntity(
    id: UUID? = null,
    createdAt: LocalDateTime = LocalDateTime.now(),
    updatedAt: LocalDateTime = createdAt,

    @Column("pet_owner_detail")
    val petOwnerDetail: PetOwnerDetail
) : AbstractEntity<UUID>(id, createdAt, updatedAt) {

    companion object {
        const val ENTITY_NAME = "pet_owner"
    }
}

@StoreJson
data class PetOwnerDetail(
    val name: String
)
