package ru.nemodev.template.repository

import org.springframework.data.domain.Pageable
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import ru.nemodev.template.entity.PetEntity
import java.util.*

interface PetCustomRepository {

    fun findAllByClientId(
        clientId: UUID,
        pageable: Pageable
    ): List<PetEntity>
}

@Repository
class PetCustomRepositoryImpl(
    private val template: JdbcTemplate
) : PetCustomRepository {

    override fun findAllByClientId(clientId: UUID, pageable: Pageable): List<PetEntity> {
//        val query = Query.query(
//            Criteria
//                .where("pet_detail ->> 'clientId'")
//                .`is`(clientId.toString())
//        ).with(pageable)

        // TODO как делать запрос
        return emptyList()//template.query(query, PetEntity::class.java)
    }
}