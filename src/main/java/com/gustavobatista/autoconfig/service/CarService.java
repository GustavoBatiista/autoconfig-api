package com.gustavobatista.autoconfig.service;

import java.util.List;

import com.gustavobatista.autoconfig.dto.CarRequestDTO;
import com.gustavobatista.autoconfig.dto.CarResponseDTO;

public interface CarService {

    public CarResponseDTO createCar(CarRequestDTO dto);

    public CarResponseDTO updateCar(Long id, CarRequestDTO dto);

    public void deleteCar(Long id);

    public List<CarResponseDTO> findAllCars();

    public CarResponseDTO findCarById(Long id);
}