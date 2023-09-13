package com.github.daniel.shuy.kotlin.spring.boot.example

import com.github.daniel.shuy.kotlin.spring.boot.example.SecurityConfig.Companion.defaultSecurityFilterChain
import com.github.daniel.shuy.kotlin.spring.boot.example.SecurityConfig.Companion.userDetailsService
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.support.beans

@SpringBootApplication
@ConfigurationPropertiesScan
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args) {
        addInitializers(
            beans {
                defaultSecurityFilterChain()
                userDetailsService()
            },
        )
    }
}
