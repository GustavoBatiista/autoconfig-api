package com.gustavobatista.autoconfig.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gustavobatista.autoconfig.entity.VehicleEntry;

@Repository
public interface VehicleEntryRepository extends JpaRepository<VehicleEntry, Long> {

    boolean existsByChassisIgnoreCase(String chassis);

    boolean existsByChassisIgnoreCaseAndIdNot(String chassis, Long id);
}