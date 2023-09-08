package com.github.daniel.shuy.kotlin.spring.boot.example.specification;

import com.github.daniel.shuy.kotlin.spring.boot.example.model.Pet;
import com.github.daniel.shuy.kotlin.spring.boot.example.model.Pet_;
import com.github.daniel.shuy.kotlin.spring.boot.example.model.Status;
import java.util.Collection;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class PetSpecifications {
  public Specification<Pet> nameLike(String namePattern) {
    return (root, query, criteriaBuilder) ->
      criteriaBuilder.like(root.get(Pet_.name), namePattern);
  }

  public Specification<Pet> statusEquals(Status status) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get(Pet_.status), status);
  }

  public Specification<Pet> tagsIn(Collection<String> tags) {
    return (root, query, criteriaBuilder) ->
        root.join(Pet_.tags).in(tags);
  }
}
