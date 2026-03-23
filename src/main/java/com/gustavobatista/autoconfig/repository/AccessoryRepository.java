package com.gustavobatista.autoconfig.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gustavobatista.autoconfig.entity.Accessory;

@Repository
public interface AccessoryRepository extends JpaRepository<Accessory, Long> {

    boolean existsByNameIgnoreCaseAndCarId_Id(String name, Long carId);

    boolean existsByNameIgnoreCaseAndCarId_IdAndIdNot(String name, Long carId, Long id);
}