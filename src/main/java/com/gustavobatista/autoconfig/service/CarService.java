package com.gustavobatista.autoconfig.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.gustavobatista.autoconfig.dto.CarRequestDTO;
import com.gustavobatista.autoconfig.dto.CarResponseDTO;

public interface CarService {

    public CarResponseDTO createCar(CarRequestDTO dto);

    public CarResponseDTO updateCar(Long id, CarRequestDTO dto);

    public void deleteCar(Long id);

    public Page<CarResponseDTO> findAllCars(Pageable pageable);

    public CarResponseDTO findCarById(Long id);
}