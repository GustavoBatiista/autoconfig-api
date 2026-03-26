package com.gustavobatista.autoconfig.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gustavobatista.autoconfig.entity.Accessory;

@Repository
public interface AccessoryRepository extends JpaRepository<Accessory, Long> {

    @EntityGraph(attributePaths = {"carId"})
    Page<Accessory> findAll(Pageable pageable);

    boolean existsByNameIgnoreCaseAndCarId_Id(String name, Long carId);

    boolean existsByNameIgnoreCaseAndCarId_IdAndIdNot(String name, Long carId, Long id);
}