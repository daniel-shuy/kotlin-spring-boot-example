package com.github.daniel.shuy.kotlin.spring.boot.example

import com.github.daniel.shuy.kotlin.spring.boot.example.dto.PetDto
import com.github.daniel.shuy.kotlin.spring.boot.example.dto.PetFilterDto
import com.github.daniel.shuy.kotlin.spring.boot.example.mapper.PetMapperImpl
import com.github.daniel.shuy.kotlin.spring.boot.example.model.Pet
import com.github.daniel.shuy.kotlin.spring.boot.example.model.Status
import com.github.daniel.shuy.kotlin.spring.boot.example.properties.PetProperties
import com.github.daniel.shuy.kotlin.spring.boot.example.repository.PetRepository
import com.github.daniel.shuy.kotlin.spring.boot.example.service.PetService
import com.github.daniel.shuy.kotlin.spring.boot.example.specification.PetSpecifications
import org.assertj.core.api.Assertions
import org.assertj.core.api.Condition
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.server.ResponseStatusException
import java.util.Optional
import java.util.concurrent.atomic.AtomicLong

@ExtendWith(SpringExtension::class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
private class PetServiceTest(
    private val petService: PetService,
) {
    companion object {
        private const val PET_NAME_MAX_LENGTH = 10

        private val SEQUENCE_PET_ID = AtomicLong()

        private val PET_1 = Pet(
            name = "Foo",
            status = Status.PENDING,
            tags = setOf("foo"),
        ).apply {
            id = SEQUENCE_PET_ID.incrementAndGet()
        }
        private val PET_2 = Pet(
            name = "Bar",
            status = Status.SOLD,
            tags = setOf("bar"),
        ).apply {
            id = SEQUENCE_PET_ID.incrementAndGet()
        }
        private val PET_3 = Pet(
            name = "FooBar",
            status = Status.SOLD,
            tags = setOf("foo", "bar"),
        ).apply {
            id = SEQUENCE_PET_ID.incrementAndGet()
        }
        private val EXISTING_PETS = listOf(PET_1, PET_2, PET_3)
    }

    @Test
    fun createPetTest() {
        // GIVEN
        val petDto = PetDto(
            name = "Pet",
            status = Status.AVALAILABLE,
        )

        // WHEN
        val createdPet = petService.createPet(petDto)

        // THEN
        Assertions.assertThat(createdPet.id)
            .isNotNull
        Assertions.assertThat(createdPet.name)
            .isEqualTo(petDto.name)
        Assertions.assertThat(createdPet.status)
            .isEqualTo(petDto.status)
    }

    @Test
    fun createPetInvalidNameFormatTest() {
        // GIVEN
        val petDto = PetDto(
            name = "X Æ A-12",
            status = Status.AVALAILABLE,
        )

        // WHEN
        Assertions.assertThatExceptionOfType(ResponseStatusException::class.java)
            .isThrownBy { petService.createPet(petDto) } // THEN
            .`is`(
                Condition(
                    { ex -> ex.statusCode == HttpStatus.BAD_REQUEST },
                    "status code Bad Request",
                ),
            )
            .withMessage("400 BAD_REQUEST \"Name can only contain alphabetic or space characters\"")
    }

    @Test
    fun createPetInvalidNameLengthTest() {
        // GIVEN
        val petDto = PetDto(
            name = "abcdefghijklmnopqrstuvwxyz",
            status = Status.AVALAILABLE,
        )

        // WHEN
        Assertions.assertThatExceptionOfType(ResponseStatusException::class.java)
            .isThrownBy { petService.createPet(petDto) } // THEN
            .`is`(
                Condition(
                    { ex -> ex.statusCode == HttpStatus.BAD_REQUEST },
                    "status code Bad Request",
                ),
            )
            .withMessage("400 BAD_REQUEST \"Name cannot be longer than %d\"", PET_NAME_MAX_LENGTH)
    }

    @Test
    fun updatePetTest() {
        // GIVEN
        val petStatus = Status.SOLD
        val petDto = PetDto(
            id = PET_1.id,
            name = PET_1.name,
            status = petStatus,
        )

        // WHEN
        val updatedPet = petService.updatePet(petDto)

        // THEN
        Assertions.assertThat(updatedPet.id)
            .isEqualTo(PET_1.id)
        Assertions.assertThat(updatedPet.name)
            .isEqualTo(PET_1.name)
        Assertions.assertThat(updatedPet.status)
            .isEqualTo(petStatus)
    }

    @Test
    fun findPetsTest() {
        // WHEN
        val pets = petService.findPets(PetFilterDto(), Pageable.unpaged())

        // THEN
        Assertions.assertThat(pets.content)
            .hasSameSizeAs(EXISTING_PETS)
    }

    @Test
    fun getPetByIdTest() {
        // WHEN
        val pet = petService.findPetById(PET_1.id!!)

        // THEN
        Assertions.assertThat(pet.id)
            .isEqualTo(PET_1.id)
        Assertions.assertThat(pet.name)
            .isEqualTo(PET_1.name)
        Assertions.assertThat(pet.status)
            .isEqualTo(PET_1.status)
    }

    @Test
    fun getPetByIdNotFoundTest() {
        // GIVEN
        val petId = SEQUENCE_PET_ID.incrementAndGet()

        // WHEN
        Assertions.assertThatExceptionOfType(ResponseStatusException::class.java)
            .isThrownBy { petService.findPetById(petId) } // THEN
            .`is`(
                Condition(
                    { ex -> ex.statusCode == HttpStatus.NOT_FOUND },
                    "status code Not Found",
                ),
            )
    }

    @TestConfiguration
    @Import(
        PetMapperImpl::class,
        PetService::class,
        PetSpecifications::class,
    )
    class Configuration {
        @Bean
        fun mockPetProperties() = PetProperties(PET_NAME_MAX_LENGTH)

        @Bean
        fun mockPetRepository(): PetRepository = mock {
            on { save(any<Pet>()) } doAnswer { invocation ->
                val pet = invocation.getArgument<Pet>(0)
                if (pet.id == null) {
                    pet.id = SEQUENCE_PET_ID.incrementAndGet()
                }
                return@doAnswer pet.clone()
            }

            on { findById(any()) } doReturn Optional.empty()
            EXISTING_PETS.forEach { pet ->
                on { findById(pet.id!!) } doReturn Optional.of(pet.clone())
            }

            on { findAll(any(), any<Pageable>()) } doReturn PageImpl(EXISTING_PETS)
                .map(Pet::clone)
        }
    }
}

fun Pet.clone() = Pet(
    name,
    status,
    tags,
).apply {
    id = this@clone.id
}
