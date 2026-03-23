package com.gustavobatista.autoconfig.service;

import java.util.List;

import com.gustavobatista.autoconfig.dto.VehicleEntryRequestDTO;
import com.gustavobatista.autoconfig.dto.VehicleEntryResponseDTO;

public interface VehicleService {

    public VehicleEntryResponseDTO createVehicle(VehicleEntryRequestDTO dto);

    public VehicleEntryResponseDTO updateVehicle(Long id, VehicleEntryRequestDTO dto);

    public void deleteVehicle(Long id);

    public List<VehicleEntryResponseDTO> findAllVehicles();

    public VehicleEntryResponseDTO findVehicleById(Long id);
}