package com.github.daniel.shuy.kotlin.spring.boot.example.mapper;

import com.github.daniel.shuy.kotlin.spring.boot.example.dto.PetDto;
import com.github.daniel.shuy.kotlin.spring.boot.example.model.Pet;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PetMapper {
  PetDto mapToDto(Pet pet);

  Pet mapToEntity(PetDto petDto);

  void updateEntityFromDto(PetDto petDto, @MappingTarget Pet pet);
}
