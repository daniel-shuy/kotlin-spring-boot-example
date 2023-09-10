package com.github.daniel.shuy.kotlin.spring.boot.example.controller

import com.github.daniel.shuy.kotlin.spring.boot.example.dto.PetDto
import com.github.daniel.shuy.kotlin.spring.boot.example.dto.PetFilterDto
import com.github.daniel.shuy.kotlin.spring.boot.example.dto.PetId
import com.github.daniel.shuy.kotlin.spring.boot.example.mapper.PetMapper
import com.github.daniel.shuy.kotlin.spring.boot.example.service.PetService
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(PetController.REQUEST_MAPPING_PATH)
@Validated
class PetController(
    private val petMapper: PetMapper,
    private val petService: PetService,
) {
    companion object {
        const val REQUEST_MAPPING_PATH = "/pet"
    }

    @PostMapping(
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun createPet(@Valid @RequestBody petDto: PetDto): ResponseEntity<PetDto> {
        val pet = petService.createPet(petDto)
        return ResponseEntity
            .ok(petMapper.mapToDto(pet))
    }

    @GetMapping(
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun findPets(
        @ParameterObject pageable: Pageable,
        @Valid @ParameterObject petFilterDto: PetFilterDto,
    ): ResponseEntity<Page<PetDto>> {
        val pets = petService.findPets(petFilterDto, pageable)
        return ResponseEntity
            .ok(pets.map(petMapper::mapToDto))
    }

    @GetMapping(
        path = ["/{id}"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun findPet(@PathVariable id: PetId): ResponseEntity<PetDto> {
        val pet = petService.findPetById(id)
        return ResponseEntity
            .ok(petMapper.mapToDto(pet))
    }

    @PutMapping(
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun updatePet(@Valid @RequestBody petDto: PetDto): ResponseEntity<PetDto> {
        val pet = petService.updatePet(petDto)
        return ResponseEntity
            .ok(petMapper.mapToDto(pet))
    }

    @DeleteMapping(path = ["/{id}"])
    fun deletePet(@PathVariable id: PetId) {
        petService.deletePet(id)
    }

    @GetMapping(
        path = ["/sold-count-by-tag"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun getSoldCountByTag(): ResponseEntity<Map<String, Number>> {
        return ResponseEntity
            .ok(petService.getSoldCountByTag())
    }
}
