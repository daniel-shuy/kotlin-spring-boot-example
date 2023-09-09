package com.github.daniel.shuy.kotlin.spring.boot.example

import com.github.daniel.shuy.kotlin.spring.boot.example.controller.PetController
import org.springdoc.core.properties.SpringDocConfigProperties
import org.springdoc.core.properties.SwaggerUiConfigParameters
import org.springframework.boot.autoconfigure.security.servlet.PathRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.csrf.CookieCsrfTokenRepository
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.web.servlet.handler.HandlerMappingIntrospector

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val springDocConfigProperties: SpringDocConfigProperties?,
    private val swaggerUiConfigParameters: SwaggerUiConfigParameters?,
) {
    companion object {
        private const val AUTHORITY_USER = "USER"
    }

    @Bean
    fun defaultSecurityFilterChain(http: HttpSecurity, introspector: HandlerMappingIntrospector): SecurityFilterChain {
        http
            .csrf { csrf ->
                csrf
                    .ignoringRequestMatchers(PathRequest.toH2Console())
                    .csrfTokenRepository(
                        CookieCsrfTokenRepository.withHttpOnlyFalse().apply {
                            cookiePath = "/"
                        },
                    )
                    .csrfTokenRequestHandler(
                        CsrfTokenRequestAttributeHandler().apply {
                            setCsrfRequestAttributeName(null)
                        },
                    )
            }
            .headers { headers ->
                headers
                    .frameOptions { frameOptions ->
                        frameOptions
                            // required for H2 Console
                            .sameOrigin()
                    }
            }
            .authorizeHttpRequests { authorize ->
                authorize
                    .requestMatchers(PathRequest.toH2Console())
                    .authenticated()

                // springdoc-openapi
                springDocConfigProperties?.apiDocs?.path?.let {
                    authorize.requestMatchers(AntPathRequestMatcher.antMatcher("$it/**"))
                        .authenticated()
                }
                authorize.requestMatchers(
                    AntPathRequestMatcher.antMatcher(
                        "${swaggerUiConfigParameters?.uiRootPath ?: ""}/swagger-ui/**",
                    ),
                )
                    .authenticated()
                swaggerUiConfigParameters?.path?.let {
                    authorize.requestMatchers(AntPathRequestMatcher.antMatcher(it))
                        .authenticated()
                }

                val mvcMatcherBuilder = MvcRequestMatcher.Builder(introspector)
                authorize
                    .requestMatchers(mvcMatcherBuilder.pattern("${PetController.REQUEST_MAPPING_PATH}/**"))
                    .hasAuthority(AUTHORITY_USER)
                    .anyRequest()
                    .denyAll()
            }
            .formLogin(Customizer.withDefaults())

        return http.build()
    }

    @Bean
    fun userDetailsService(): UserDetailsService {
        val userDetails = User.withDefaultPasswordEncoder()
            .username("user")
            .password("password")
            .authorities(AUTHORITY_USER)
            .build()
        return InMemoryUserDetailsManager(userDetails)
    }
}
