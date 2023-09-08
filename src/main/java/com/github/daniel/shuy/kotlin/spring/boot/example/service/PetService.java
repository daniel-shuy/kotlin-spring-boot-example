package com.github.daniel.shuy.kotlin.spring.boot.example.service;

import com.github.daniel.shuy.kotlin.spring.boot.example.dto.PetDto;
import com.github.daniel.shuy.kotlin.spring.boot.example.dto.PetFilterDto;
import com.github.daniel.shuy.kotlin.spring.boot.example.mapper.PetMapper;
import com.github.daniel.shuy.kotlin.spring.boot.example.model.Pet;
import com.github.daniel.shuy.kotlin.spring.boot.example.properties.PetProperties;
import com.github.daniel.shuy.kotlin.spring.boot.example.repository.PetRepository;
import com.github.daniel.shuy.kotlin.spring.boot.example.specification.PetSpecifications;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class PetService {
  private static final Pattern REGEX_ALPHABETIC_OR_SPACE = Pattern.compile("[a-zA-Z ]*");

  private final PetMapper petMapper;
  private final PetProperties petProperties;
  private final PetRepository petRepository;
  private final PetSpecifications petSpecifications;

  public Pet createPet(PetDto petDto) {
    validateName(petDto);

    var pet = petMapper.mapToEntity(petDto);

    var savedPet = petRepository.save(pet);
    log.info("Created Pet with id {}", savedPet.getId());
    return savedPet;
  }

  public Page<Pet> findPets(PetFilterDto petFilterDto, Pageable pageable) {
    var specification = petFilterDto.toSpecification(petSpecifications);
    return petRepository.findAll(specification, pageable);
  }

  public Pet findPetById(Long petId) {
    return petRepository.findById(petId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  }

  public Pet updatePet(PetDto petDto) {
    var pet = findPetById(petDto.id());

    validateName(petDto);

    petMapper.updateEntityFromDto(petDto, pet);

    var savedPet = petRepository.save(pet);
    log.info("Updated Pet with id {}", savedPet.getId());
    return savedPet;
  }

  public void deletePet(Long petId) {
    var pet = findPetById(petId);

    petRepository.delete(pet);
    log.info("Deleted Pet with id {}", pet.getId());
  }

  public Map<String, Number> getSoldCountByTag() {
    return petRepository.getSoldCountByTag();
  }

  protected void validateName(PetDto petDto) {
    var name = petDto.name();
    if (name == null || name.isEmpty()) {
      return;
    }

    var nameMaxLength = petProperties.nameMaxLength();
    if (nameMaxLength != null && name.length() > nameMaxLength) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Name cannot be longer than %d", nameMaxLength));
    }

    if (!REGEX_ALPHABETIC_OR_SPACE.matcher(name).matches()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name can only contain alphabetic or space characters");
    }
  }
}
