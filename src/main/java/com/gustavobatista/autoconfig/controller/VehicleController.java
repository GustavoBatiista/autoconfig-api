package com.gustavobatista.autoconfig.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gustavobatista.autoconfig.dto.VehicleEntryRequestDTO;
import com.gustavobatista.autoconfig.dto.VehicleEntryResponseDTO;
import com.gustavobatista.autoconfig.service.VehicleService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/vehicles")
@Tag(name = "6 - Vehicles", description = "Endpoints for vehicle management")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping
    public ResponseEntity<VehicleEntryResponseDTO> create(@Valid @RequestBody VehicleEntryRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleService.createVehicle(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehicleEntryResponseDTO> update(@PathVariable Long id, @Valid @RequestBody VehicleEntryRequestDTO dto) {
        return ResponseEntity.ok(vehicleService.updateVehicle(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<VehicleEntryResponseDTO>> findAll() {
        return ResponseEntity.ok(vehicleService.findAllVehicles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleEntryResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.findVehicleById(id));
    }
}