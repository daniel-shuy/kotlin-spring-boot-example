package com.github.daniel.shuy.kotlin.spring.boot.example.repository

import com.github.daniel.shuy.kotlin.spring.boot.example.model.Pet
import com.github.daniel.shuy.kotlin.spring.boot.example.model.Pet_
import com.github.daniel.shuy.kotlin.spring.boot.example.model.Status
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.persistence.Tuple

class CustomPetRepositoryImpl : CustomPetRepository {
    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override fun getSoldCountByTag(): Map<String, Number> {
        val criteriaBuilder = entityManager.criteriaBuilder
        val criteriaQuery = criteriaBuilder.createQuery(Tuple::class.java)
        val root = criteriaQuery.from(Pet::class.java)
        criteriaQuery.where(criteriaBuilder.equal(root.get(Pet_.status), Status.SOLD))
            .groupBy(root.get(Pet_.tags))
            .multiselect(root.get(Pet_.tags), criteriaBuilder.count(root))
        val resultList = entityManager.createQuery(criteriaQuery)
            .resultList
        return resultList
            .associate { it.get(0, String::class.java) to it.get(1, Number::class.java) }
    }
}
