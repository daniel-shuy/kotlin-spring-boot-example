package com.github.daniel.shuy.kotlin.spring.boot.example.specification

import com.github.daniel.shuy.kotlin.spring.boot.example.model.Pet_
import com.github.daniel.shuy.kotlin.spring.boot.example.model.Status
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Component

@Component
class PetSpecifications {
    fun nameLike(namePattern: String) = Specification { root, query, criteriaBuilder ->
        criteriaBuilder.like(root.get(Pet_.name), namePattern)
    }

    fun statusEquals(status: Status) = Specification { root, query, criteriaBuilder ->
        criteriaBuilder.equal(root.get(Pet_.status), status)
    }

    fun tagsIn(tags: Collection<String>) = Specification { root, query, criteriaBuilder ->
        root.join(Pet_.tags).`in`(tags)
    }
}
