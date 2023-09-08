package com.github.daniel.shuy.kotlin.spring.boot.example.dto;

import com.github.daniel.shuy.kotlin.spring.boot.example.model.Pet;
import com.github.daniel.shuy.kotlin.spring.boot.example.model.Status;
import com.github.daniel.shuy.kotlin.spring.boot.example.specification.PetSpecifications;
import java.util.Collection;
import lombok.Builder;
import org.springframework.data.jpa.domain.Specification;

@Builder
public record PetFilterDto(
    String namePattern,
    Status status,
    Collection<String> tags) {
  public Specification<Pet> toSpecification(PetSpecifications petSpecifications) {
    Specification<Pet> specification = Specification.where(null);
    if (namePattern != null && !namePattern.isEmpty()) {
      specification = specification.and(petSpecifications.nameLike(namePattern));
    }
    if (status != null) {
      specification = specification.and(petSpecifications.statusEquals(status));
    }
    if (tags != null && !tags.isEmpty()) {
      specification = specification.and(petSpecifications.tagsIn(tags));
    }
    return specification;
  }
}
