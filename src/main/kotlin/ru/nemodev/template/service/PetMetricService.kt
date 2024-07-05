package ru.nemodev.template.service

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import org.springframework.stereotype.Service
import ru.nemodev.template.entity.PetEntity

interface PetMetricService {
    fun created(pet: PetEntity)
}

@Service
class PetMetricServiceImpl(
    private val meterRegistry: MeterRegistry
) : PetMetricService {

    override fun created(pet: PetEntity) {
        meterRegistry
            .counter("pet_created", listOf(Tag.of("type", pet.petDetail.type.name)))
            .increment()
    }
}