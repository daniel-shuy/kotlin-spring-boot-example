package com.github.daniel.shuy.kotlin.spring.boot.example.controller;

import com.github.daniel.shuy.kotlin.spring.boot.example.dto.PetDto;
import com.github.daniel.shuy.kotlin.spring.boot.example.dto.PetFilterDto;
import com.github.daniel.shuy.kotlin.spring.boot.example.mapper.PetMapper;
import com.github.daniel.shuy.kotlin.spring.boot.example.service.PetService;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.github.daniel.shuy.kotlin.spring.boot.example.controller.PetController.REQUEST_MAPPING_PATH;

@RestController
@RequestMapping(REQUEST_MAPPING_PATH)
@Validated
@RequiredArgsConstructor
public class PetController {
  public static final String REQUEST_MAPPING_PATH = "/pet";

  private final PetMapper petMapper;
  private final PetService petService;

  @PostMapping(
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<PetDto> createPet(@Valid @RequestBody PetDto petDto) {
    var pet = petService.createPet(petDto);
    return ResponseEntity
        .ok(petMapper.mapToDto(pet));
  }

  @GetMapping(
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Page<PetDto>> findPets(@ParameterObject Pageable pageable, @Valid @ParameterObject PetFilterDto petFilterDto) {
    var pets = petService.findPets(petFilterDto, pageable);
    return ResponseEntity
        .ok(pets.map(petMapper::mapToDto));
  }

  @GetMapping(path = "/{id}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<PetDto> findPet(@PathVariable Long id) {
    var pet = petService.findPetById(id);
    return ResponseEntity
        .ok(petMapper.mapToDto(pet));
  }

  @PutMapping(
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<PetDto> updatePet(@Valid @RequestBody PetDto petDto) {
    var pet = petService.updatePet(petDto);
    return ResponseEntity
        .ok(petMapper.mapToDto(pet));
  }

  @DeleteMapping(path = "/{id}")
  public void deletePet(@PathVariable Long id) {
    petService.deletePet(id);
  }

  @GetMapping(path = "/sold-count-by-tag",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Map<String, Number>> getSoldCountByTag() {
    return ResponseEntity
        .ok(petService.getSoldCountByTag());
  }
}
