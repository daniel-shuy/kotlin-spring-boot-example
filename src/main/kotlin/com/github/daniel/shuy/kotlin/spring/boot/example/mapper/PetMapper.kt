package com.github.daniel.shuy.kotlin.spring.boot.example.mapper

import com.github.daniel.shuy.kotlin.spring.boot.example.dto.PetDto
import com.github.daniel.shuy.kotlin.spring.boot.example.model.Pet
import org.mapstruct.Mapper
import org.mapstruct.MappingConstants
import org.mapstruct.MappingTarget

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface PetMapper {
    fun mapToDto(pet: Pet): PetDto
    fun mapToEntity(petDto: PetDto): Pet
    fun updateEntityFromDto(petDto: PetDto, @MappingTarget pet: Pet)
}
