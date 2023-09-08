package com.github.daniel.shuy.kotlin.spring.boot.example.repository;

import com.github.daniel.shuy.kotlin.spring.boot.example.model.Pet;
import com.github.daniel.shuy.kotlin.spring.boot.example.model.Pet_;
import com.github.daniel.shuy.kotlin.spring.boot.example.model.Status;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import java.util.Map;
import java.util.stream.Collectors;

public class CustomPetRepositoryImpl implements CustomPetRepository {
  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Map<String, Number> getSoldCountByTag() {
    var criteriaBuilder = entityManager.getCriteriaBuilder();
    var criteriaQuery = criteriaBuilder.createQuery(Tuple.class);
    var root = criteriaQuery.from(Pet.class);
    criteriaQuery.where(criteriaBuilder.equal(root.get(Pet_.status), Status.SOLD))
        .groupBy(root.get(Pet_.tags))
        .multiselect(root.get(Pet_.tags), criteriaBuilder.count(root));
    var resultList = entityManager.createQuery(criteriaQuery)
        .getResultList();
    return resultList.stream()
        .collect(Collectors.toMap(
            tuple -> tuple.get(0, String.class),
            tuple -> tuple.get(1, Number.class)));
  }
}
