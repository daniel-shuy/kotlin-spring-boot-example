package com.github.daniel.shuy.kotlin.spring.boot.example;

import com.github.daniel.shuy.kotlin.spring.boot.example.controller.PetController;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigParameters;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
  private static final String AUTHORITY_USER = "USER";

  private final Optional<SpringDocConfigProperties> springDocConfigProperties;
  private final Optional<SwaggerUiConfigParameters> swaggerUiConfigParameters;

  @Bean
  public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http, HandlerMappingIntrospector introspector)
      throws Exception {
    http
        .csrf(csrf -> {
          csrf.ignoringRequestMatchers(PathRequest.toH2Console());

          var requestHandler = new CsrfTokenRequestAttributeHandler();
          requestHandler.setCsrfRequestAttributeName(null);
          var csrfTokenRepository = CookieCsrfTokenRepository
              .withHttpOnlyFalse();
          csrfTokenRepository.setCookiePath("/");
          csrf.csrfTokenRepository(csrfTokenRepository)
              .csrfTokenRequestHandler(requestHandler);
        })

        .headers(headers -> headers
            // required for H2 Console
            .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin
            )
        )

        .authorizeHttpRequests(authorize -> {
          authorize
              .requestMatchers(PathRequest.toH2Console())
              .permitAll();

          // springdoc-openapi
          springDocConfigProperties
              .map(SpringDocConfigProperties::getApiDocs)
              .map(SpringDocConfigProperties.ApiDocs::getPath)
              .ifPresent(springDocApiDocsPath -> authorize
                  .requestMatchers(AntPathRequestMatcher.antMatcher(anyPathAfter(springDocApiDocsPath)))
                  .authenticated());
          authorize
              .requestMatchers(AntPathRequestMatcher.antMatcher(anyPathAfter(swaggerUiConfigParameters
                  .map(SwaggerUiConfigParameters::getUiRootPath)
                  .orElse("") + "/swagger-ui")))
              .authenticated();
          swaggerUiConfigParameters
              .map(AbstractSwaggerUiConfigProperties::getPath)
              .ifPresent(swaggerUiPath -> authorize
                  .requestMatchers(AntPathRequestMatcher.antMatcher(swaggerUiPath))
                  .authenticated());

          var mvcMatcherBuilder = new MvcRequestMatcher.Builder(introspector);
          authorize
              .requestMatchers(mvcMatcherBuilder.pattern(anyPathAfter(PetController.REQUEST_MAPPING_PATH)))
              .hasAuthority(AUTHORITY_USER)

              .anyRequest()
              .denyAll();
        })

        .formLogin(Customizer.withDefaults());

    return http.build();
  }

  @Bean
  public UserDetailsService userDetailsService() {
    UserDetails userDetails = User.withDefaultPasswordEncoder()
        .username("user")
        .password("password")
        .authorities(AUTHORITY_USER)
        .build();

    return new InMemoryUserDetailsManager(userDetails);
  }

  private String anyPathAfter(String pattern) {
    return pattern + "/**";
  }
}
