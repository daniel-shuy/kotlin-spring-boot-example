package com.github.daniel.shuy.kotlin.spring.boot.example.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@ConfigurationProperties("pet")
@Validated
data class PetProperties(
    val nameMaxLength: Int?,
)
