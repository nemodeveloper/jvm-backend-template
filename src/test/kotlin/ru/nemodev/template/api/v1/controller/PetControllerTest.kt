package ru.nemodev.template.api.v1.controller

import org.springframework.core.ParameterizedTypeReference
import ru.nemodev.platform.core.api.dto.paging.PageDtoRs
import ru.nemodev.platform.core.api.headers.ApiHeaderNames
import ru.nemodev.template.api.v1.dto.PetCreateDtoRq
import ru.nemodev.template.api.v1.dto.PetCreateDtoRs
import ru.nemodev.template.api.v1.dto.PetDetailDtoRs
import ru.nemodev.template.api.v1.dto.PetTypeDto
import java.util.*

//@ActiveProfiles("it")
class PetControllerTest : BaseIntegrationTest() {

    companion object {
        private const val BASE_API_URL = "/v1/pets"
        private const val PET_DETAIL_URL = "$BASE_API_URL/{id}"

        val petForCreate = PetCreateDtoRq(
            name = "НеМурка",
            type = PetTypeDto.CAT
        )
    }

//    @Test
    fun create_Success() {
        val clientId = UUID.randomUUID().toString()

        webClient
            .post()
            .uri(BASE_API_URL)
            .header(ApiHeaderNames.USER_ID, clientId)
            .bodyValue(petForCreate)
            .exchange()
            .expectStatus().isCreated
            .expectBody(PetCreateDtoRs::class.java).returnResult().responseBody!!
    }

//    @Test
    fun create_NoUserId_BadRequestError() {
        webClient
            .post()
            .uri(BASE_API_URL)
            .bodyValue(petForCreate)
            .exchange()
            .expectStatus().isBadRequest
    }

//    @Test
    fun create_NoApiKey_UnauthorizedError() {
        val clientId = UUID.randomUUID().toString()

        webClient
            .post()
            .uri(BASE_API_URL)
            .header(ApiHeaderNames.USER_ID, clientId)
            .bodyValue(petForCreate)
            .exchange()
            .expectStatus().isUnauthorized
    }

//    @Test
    fun successFlow() {
        val clientId = UUID.randomUUID().toString()
        val petForCreate = PetCreateDtoRq(
            name = "НеМурка",
            type = PetTypeDto.CAT
        )

        val petCreated = webClient
            .post()
            .uri(BASE_API_URL)
            .header(ApiHeaderNames.USER_ID, clientId)
            .bodyValue(petForCreate)
            .exchange()
            .expectStatus().isCreated
            .expectBody(PetCreateDtoRs::class.java).returnResult().responseBody!!

        val petDetail = webClient
            .get()
            .uri(PET_DETAIL_URL, petCreated.id)
            .header(ApiHeaderNames.USER_ID, clientId)
            .exchange()
            .expectStatus().is2xxSuccessful
            .expectBody(PetDetailDtoRs::class.java).returnResult().responseBody!!
        assert(petForCreate.name == petDetail.name)
        assert(petForCreate.type == petDetail.type)

        val pets = webClient
            .get()
            .uri(BASE_API_URL)
            .header(ApiHeaderNames.USER_ID, clientId)
            .exchange()
            .expectStatus().is2xxSuccessful
            .expectBody(object : ParameterizedTypeReference<PageDtoRs<PetDetailDtoRs>>(){}).returnResult().responseBody!!
        assert(pets.items.size == 1)
    }
}