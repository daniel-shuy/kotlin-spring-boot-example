package com.github.daniel.shuy.kotlin.spring.boot.example.service

import com.github.daniel.shuy.kotlin.spring.boot.example.dto.PetDto
import com.github.daniel.shuy.kotlin.spring.boot.example.dto.PetFilterDto
import com.github.daniel.shuy.kotlin.spring.boot.example.mapper.PetMapper
import com.github.daniel.shuy.kotlin.spring.boot.example.model.Pet
import com.github.daniel.shuy.kotlin.spring.boot.example.properties.PetProperties
import com.github.daniel.shuy.kotlin.spring.boot.example.repository.PetRepository
import com.github.daniel.shuy.kotlin.spring.boot.example.specification.PetSpecifications
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

private val logger = KotlinLogging.logger {}

@Service
class PetService(
    private val petMapper: PetMapper,
    private val petProperties: PetProperties,
    private val petRepository: PetRepository,
    private val petSpecifications: PetSpecifications,
) {
    companion object {
        private val REGEX_ALPHABETIC_OR_SPACE = Regex("[a-zA-Z ]*")
    }

    fun createPet(petDto: PetDto): Pet {
        validateName(petDto)
        val pet = petMapper.mapToEntity(petDto)
        return petRepository.save(pet).also {
            logger.info { "Created Pet with id ${it.id}" }
        }
    }

    fun findPets(petFilterDto: PetFilterDto, pageable: Pageable): Page<Pet> {
        val specification = petFilterDto.toSpecification(petSpecifications)
        return petRepository.findAll(specification, pageable)
    }

    fun findPetById(petId: Long): Pet {
        return petRepository.findByIdOrNull(petId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    fun updatePet(petDto: PetDto): Pet {
        val petId = petDto.id
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id must not be null")
        val pet = findPetById(petId)
        validateName(petDto)
        petMapper.updateEntityFromDto(petDto, pet)
        return petRepository.save(pet).also {
            logger.info { "Updated Pet with id ${it.id}" }
        }
    }

    fun deletePet(petId: Long) {
        val pet = findPetById(petId)
        petRepository.delete(pet)
        logger.info { "Deleted Pet with id ${pet.id}" }
    }

    fun getSoldCountByTag(): Map<String, Number> {
        return petRepository.getSoldCountByTag()
    }

    protected fun validateName(petDto: PetDto) {
        val name = petDto.name
        if (name.isEmpty()) {
            return
        }

        petProperties.nameMaxLength?.let { nameMaxLength ->
            if (name.length > nameMaxLength) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Name cannot be longer than $nameMaxLength")
            }
        }

        if (!REGEX_ALPHABETIC_OR_SPACE.matches(name)) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Name can only contain alphabetic or space characters",
            )
        }
    }
}
