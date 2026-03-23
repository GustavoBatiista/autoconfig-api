package com.gustavobatista.autoconfig.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gustavobatista.autoconfig.entity.Car;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {

    boolean existsByBrandIgnoreCaseAndModelIgnoreCaseAndVersionIgnoreCase(
            String brand, String model, String version);

    boolean existsByBrandIgnoreCaseAndModelIgnoreCaseAndVersionIgnoreCaseAndIdNot(
            String brand, String model, String version, Long id);
}
