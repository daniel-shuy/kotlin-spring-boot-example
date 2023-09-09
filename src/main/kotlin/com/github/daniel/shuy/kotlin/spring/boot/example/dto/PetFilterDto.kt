package com.github.daniel.shuy.kotlin.spring.boot.example.dto

import com.github.daniel.shuy.kotlin.spring.boot.example.model.Pet
import com.github.daniel.shuy.kotlin.spring.boot.example.model.Status
import com.github.daniel.shuy.kotlin.spring.boot.example.specification.PetSpecifications
import org.springframework.data.jpa.domain.Specification

data class PetFilterDto(
    val namePattern: String? = null,
    val status: Status? = null,
    val tags: Collection<String>? = null,
) {
    fun toSpecification(petSpecifications: PetSpecifications): Specification<Pet> {
        var specification = Specification.where<Pet>(null)
        if (!namePattern.isNullOrEmpty()) {
            specification = specification.and(petSpecifications.nameLike(namePattern))
        }
        if (status != null) {
            specification = specification.and(petSpecifications.statusEquals(status))
        }
        if (!tags.isNullOrEmpty()) {
            specification = specification.and(petSpecifications.tagsIn(tags))
        }
        return specification
    }
}
