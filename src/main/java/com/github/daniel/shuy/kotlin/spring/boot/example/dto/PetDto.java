package com.github.daniel.shuy.kotlin.spring.boot.example.dto;

import com.github.daniel.shuy.kotlin.spring.boot.example.model.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import lombok.Builder;

@Builder
public record PetDto(
    Long id,
    @NotBlank String name,
    @NotNull Status status,
    Collection<String> tags) {
}
