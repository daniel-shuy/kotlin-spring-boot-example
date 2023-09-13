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
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldBeSameSizeAs
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldNotBeNull
import io.mockk.every
import io.mockk.mockk
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestConstructor
import org.springframework.web.server.ResponseStatusException
import java.util.concurrent.atomic.AtomicLong

@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class PetServiceTest(
    private val petService: PetService,
) : ShouldSpec() {
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

    init {
        context("PetService") {
            should("Create Pet") {
                val petDto = PetDto(
                    name = "Pet",
                    status = Status.AVALAILABLE,
                )
                petService.createPet(petDto).run {
                    id.shouldNotBeNull()
                    name.shouldBeEqual(petDto.name)
                    status.shouldBeEqual(petDto.status)
                }
            }

            should("Throw exception on creating Pet with invalid name format") {
                shouldThrow<ResponseStatusException> {
                    petService.createPet(
                        PetDto(
                            name = "X Æ A-12",
                            status = Status.AVALAILABLE,
                        ),
                    )
                }.run {
                    statusCode.shouldBeEqual(HttpStatus.BAD_REQUEST)
                    reason
                        .shouldNotBeNull()
                        .shouldBeEqual("Name can only contain alphabetic or space characters")
                }
            }

            should("Throw exception on creating Pet with invalid name length") {
                shouldThrow<ResponseStatusException> {
                    petService.createPet(
                        PetDto(
                            name = "abcdefghijklmnopqrstuvwxyz",
                            status = Status.AVALAILABLE,
                        ),
                    )
                }.run {
                    statusCode.shouldBeEqual(HttpStatus.BAD_REQUEST)
                    reason
                        .shouldNotBeNull()
                        .shouldBeEqual("Name cannot be longer than $PET_NAME_MAX_LENGTH")
                }
            }

            should("Update Pet") {
                val petStatus = Status.SOLD
                petService.updatePet(
                    PetDto(
                        id = PET_1.id,
                        name = PET_1.name,
                        status = petStatus,
                    ),
                ).run {
                    id
                        .shouldNotBeNull()
                        .shouldBeEqual(PET_1.id!!)
                    name.shouldBeEqual(PET_1.name)
                    status.shouldBeEqual(petStatus)
                }
            }

            should("Find Pets") {
                petService.findPets(PetFilterDto(), Pageable.unpaged()).run {
                    content.shouldBeSameSizeAs(EXISTING_PETS)
                }
            }

            should("Get Pet by ID") {
                petService.findPetById(PET_1.id!!).run {
                    id
                        .shouldNotBeNull()
                        .shouldBeEqual(PET_1.id!!)
                    name.shouldBeEqual(PET_1.name)
                    status.shouldBeEqual(PET_1.status)
                }
            }

            should("Throw exception on getting Pet with non-existent ID") {
                shouldThrow<ResponseStatusException> {
                    petService.findPetById(SEQUENCE_PET_ID.incrementAndGet())
                }.run {
                    statusCode.shouldBeEqual(HttpStatus.NOT_FOUND)
                }
            }
        }
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
        fun mockPetRepository(): PetRepository = mockk {
            every { save(any()) } answers {
                val pet = it.invocation.args[0] as Pet
                if (pet.id == null) {
                    pet.id = SEQUENCE_PET_ID.incrementAndGet()
                }
                return@answers pet.clone()
            }

            every { findByIdOrNull(any()) } answers { null }
            EXISTING_PETS.forEach { pet ->
                every { findByIdOrNull(pet.id!!) } answers { pet.clone() }
            }

            every { findAll(any(), any<Pageable>()) } answers {
                PageImpl(EXISTING_PETS)
                    .map(Pet::clone)
            }
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
