package ru.nemodev.template.repository

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import ru.nemodev.template.entity.PetEntity
import java.util.*

@Repository
interface PetRepository : ListCrudRepository<PetEntity, UUID> {

    @Query(
        """
        SELECT * FROM pets
            WHERE pet_detail ->> 'clientId' = :clientId
            ORDER BY created_at DESC 
            OFFSET :offset LIMIT :limit
        """
    )
    fun findAllByClientId(
        clientId: String,
        offset: Long,
        limit: Int
    ): List<PetEntity>
}
