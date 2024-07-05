package ru.nemodev.template.repository

import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import ru.nemodev.template.entity.PetOwnerEntity
import java.util.*

@Repository
interface PetOwnerRepository : ListCrudRepository<PetOwnerEntity, UUID>
