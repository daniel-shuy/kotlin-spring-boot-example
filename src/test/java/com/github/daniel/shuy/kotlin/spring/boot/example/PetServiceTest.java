package com.github.daniel.shuy.kotlin.spring.boot.example;

import com.github.daniel.shuy.kotlin.spring.boot.example.dto.PetDto;
import com.github.daniel.shuy.kotlin.spring.boot.example.dto.PetFilterDto;
import com.github.daniel.shuy.kotlin.spring.boot.example.mapper.PetMapperImpl;
import com.github.daniel.shuy.kotlin.spring.boot.example.model.Pet;
import com.github.daniel.shuy.kotlin.spring.boot.example.model.Status;
import com.github.daniel.shuy.kotlin.spring.boot.example.properties.PetProperties;
import com.github.daniel.shuy.kotlin.spring.boot.example.repository.PetRepository;
import com.github.daniel.shuy.kotlin.spring.boot.example.service.PetService;
import com.github.daniel.shuy.kotlin.spring.boot.example.specification.PetSpecifications;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(SpringExtension.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class PetServiceTest {
  private static final int PET_NAME_MAX_LENGTH = 10;

  private static final AtomicLong SEQUENCE_PET_ID = new AtomicLong();

  private static final Pet PET_1 = Pet.builder()
      .id(SEQUENCE_PET_ID.incrementAndGet())
      .name("Foo")
      .status(Status.PENDING)
      .tags(Set.of("foo"))
      .build();

  private static final Pet PET_2 = Pet.builder()
      .id(SEQUENCE_PET_ID.incrementAndGet())
      .name("Bar")
      .status(Status.SOLD)
      .tags(Set.of("bar"))
      .build();

  private static final Pet PET_3 = Pet.builder()
      .id(SEQUENCE_PET_ID.incrementAndGet())
      .name("FooBar")
      .status(Status.SOLD)
      .tags(Set.of("foo", "bar"))
      .build();

  private static final List<Pet> EXISTING_PETS = List.of(PET_1, PET_2, PET_3);

  private final PetService petService;

  @Test
  void createPetTest() {
    // GIVEN
    var petDto = PetDto.builder()
        .name("Pet")
        .status(Status.AVALAILABLE)
        .build();

    // WHEN
    var createdPet = petService.createPet(petDto);

    // THEN
    Assertions.assertThat(createdPet.getId())
        .isNotNull();
    Assertions.assertThat(createdPet.getName())
        .isEqualTo(petDto.name());
    Assertions.assertThat(createdPet.getStatus())
        .isEqualTo(petDto.status());
  }

  @Test
  void createPetInvalidNameFormatTest() {
    // GIVEN
    var petDto = PetDto.builder()
        .name("X Æ A-12")
        .status(Status.AVALAILABLE)
        .build();

    // WHEN
    Assertions.assertThatExceptionOfType(ResponseStatusException.class)
        .isThrownBy(() -> petService.createPet(petDto))
        // THEN
        .is(new Condition<>(ex -> ex.getStatusCode() == HttpStatus.BAD_REQUEST, "status code Bad Request"))
        .withMessage("400 BAD_REQUEST \"Name can only contain alphabetic or space characters\"");
  }

  @Test
  void createPetInvalidNameLengthTest() {
    // GIVEN
    var petDto = PetDto.builder()
        .name("abcdefghijklmnopqrstuvwxyz")
        .status(Status.AVALAILABLE)
        .build();

    // WHEN
    Assertions.assertThatExceptionOfType(ResponseStatusException.class)
        .isThrownBy(() -> petService.createPet(petDto))
        // THEN
        .is(new Condition<>(ex -> ex.getStatusCode() == HttpStatus.BAD_REQUEST, "status code Bad Request"))
        .withMessage("400 BAD_REQUEST \"Name cannot be longer than %d\"", PET_NAME_MAX_LENGTH);
  }

  @Test
  void updatePetTest() {
    // GIVEN
    var petStatus = Status.SOLD;
    var petDto = PetDto.builder()
        .id(PET_1.getId())
        .name(PET_1.getName())
        .status(petStatus)
        .build();

    // WHEN
    var updatedPet = petService.updatePet(petDto);

    // THEN
    Assertions.assertThat(updatedPet.getId())
        .isEqualTo(PET_1.getId());
    Assertions.assertThat(updatedPet.getName())
        .isEqualTo(PET_1.getName());
    Assertions.assertThat(updatedPet.getStatus())
        .isEqualTo(petStatus);
  }

  @Test
  void findPetsTest() {
    // WHEN
    var petFilter = PetFilterDto.builder().build();
    var pets = petService.findPets(petFilter, Pageable.unpaged());

    // THEN
    Assertions.assertThat(pets.getContent())
        .hasSameSizeAs(EXISTING_PETS);
  }

  @Test
  void getPetByIdTest() {
    // WHEN
    var pet = petService.findPetById(PET_1.getId());

    // THEN
    Assertions.assertThat(pet.getId())
        .isEqualTo(PET_1.getId());
    Assertions.assertThat(pet.getName())
        .isEqualTo(PET_1.getName());
    Assertions.assertThat(pet.getStatus())
        .isEqualTo(PET_1.getStatus());
  }

  @Test
  void getPetByIdNotFoundTest() {
    // GIVEN
    var petId = SEQUENCE_PET_ID.incrementAndGet();

    // WHEN
    Assertions.assertThatExceptionOfType(ResponseStatusException.class)
        .isThrownBy(() -> petService.findPetById(petId))
        // THEN
        .is(new Condition<>(ex -> ex.getStatusCode() == HttpStatus.NOT_FOUND, "status code Not Found"));
  }

  @TestConfiguration
  @Import({
      PetMapperImpl.class,
      PetService.class,
      PetSpecifications.class,
  })
  static class Configuration {
    @Bean
    public PetProperties mockPetProperties() {
      return new PetProperties(PET_NAME_MAX_LENGTH);
    }

    @Bean
    public PetRepository mockPetRepository() {
      var mockPetRepository = Mockito.mock(PetRepository.class);

      Mockito.when(mockPetRepository.save(Mockito.any(Pet.class)))
          .thenAnswer(invocation -> {
            var pet = invocation.getArgument(0, Pet.class);
            if (pet.getId() == null) {
              pet.setId(SEQUENCE_PET_ID.incrementAndGet());
            }
            return pet.toBuilder().build();
          });

      Mockito.when(mockPetRepository.findById(Mockito.anyLong()))
          .thenReturn(Optional.empty());
      EXISTING_PETS.forEach(pet ->
          Mockito.when(mockPetRepository.findById(pet.getId()))
              .thenReturn(Optional.of(pet.toBuilder().build())));

      Mockito.when(mockPetRepository.findAll(Mockito.<Specification<Pet>>any(), Mockito.any(Pageable.class)))
          .thenReturn(new PageImpl<>(EXISTING_PETS).map(pet -> pet.toBuilder().build()));

      return mockPetRepository;
    }
  }
}
