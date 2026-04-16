package com.gustavobatista.autoconfig.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gustavobatista.autoconfig.entity.VehicleEntry;

@Repository
public interface VehicleEntryRepository extends JpaRepository<VehicleEntry, Long> {

    Optional<VehicleEntry> findByOrderId_Id(Long orderId);

    boolean existsByChassisIgnoreCase(String chassis);

    boolean existsByChassisIgnoreCaseAndIdNot(String chassis, Long id);
}