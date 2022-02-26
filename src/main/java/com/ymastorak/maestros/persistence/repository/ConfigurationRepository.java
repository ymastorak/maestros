package com.ymastorak.maestros.persistence.repository;

import com.ymastorak.maestros.persistence.model.ConfigurationProperty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfigurationRepository extends JpaRepository<ConfigurationProperty, String> {
    Optional<ConfigurationProperty> getByName(String name);
}
