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

import com.gustavobatista.autoconfig.dto.CarRequestDTO;
import com.gustavobatista.autoconfig.dto.CarResponseDTO;
import com.gustavobatista.autoconfig.service.CarService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/cars")
public class CarController {

    private final CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;
    }

    @PostMapping
    public ResponseEntity<CarResponseDTO> create(@Valid @RequestBody CarRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(carService.createCar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CarResponseDTO> update(@PathVariable Long id, @Valid @RequestBody CarRequestDTO dto) {
        return ResponseEntity.ok(carService.updateCar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        carService.deleteCar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<CarResponseDTO>> findAll() {
        return ResponseEntity.ok(carService.findAllCars());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(carService.findCarById(id));
    }
}

