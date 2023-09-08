package com.github.daniel.shuy.kotlin.spring.boot.example.repository;

import java.util.Map;

public interface CustomPetRepository {
  Map<String, Number> getSoldCountByTag();
}
