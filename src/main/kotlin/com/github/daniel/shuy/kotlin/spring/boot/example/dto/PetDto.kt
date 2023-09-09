package com.github.daniel.shuy.kotlin.spring.boot.example.dto

import com.github.daniel.shuy.kotlin.spring.boot.example.model.Status
import jakarta.validation.constraints.NotBlank

data class PetDto(
    val id: Long? = null,
    @field:NotBlank val name: String,
    val status: Status,
    val tags: Collection<String>? = null,
)
