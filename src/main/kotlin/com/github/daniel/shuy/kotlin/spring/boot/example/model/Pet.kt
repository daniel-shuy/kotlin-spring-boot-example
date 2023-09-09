package com.github.daniel.shuy.kotlin.spring.boot.example.model

import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id

@Entity
class Pet(
    var name: String,

    @Enumerated(EnumType.STRING)
    var status: Status,

    @ElementCollection(fetch = FetchType.LAZY)
    @Column(name = "tag")
    var tags: Set<String>? = null,
) {
    @Id
    @GeneratedValue
    var id: Long? = null
}
