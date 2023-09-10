package com.github.daniel.shuy.kotlin.spring.boot.example.repository

import com.github.daniel.shuy.kotlin.spring.boot.example.dto.PetId
import com.github.daniel.shuy.kotlin.spring.boot.example.model.Pet
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface PetRepository : JpaRepository<Pet, PetId>, JpaSpecificationExecutor<Pet>, CustomPetRepository
