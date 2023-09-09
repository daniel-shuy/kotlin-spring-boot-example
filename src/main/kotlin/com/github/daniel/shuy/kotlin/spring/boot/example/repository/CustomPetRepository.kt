package com.github.daniel.shuy.kotlin.spring.boot.example.repository

interface CustomPetRepository {
    fun getSoldCountByTag(): Map<String, Number>
}
