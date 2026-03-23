package com.gustavobatista.autoconfig.service;

import java.util.List;

import com.gustavobatista.autoconfig.dto.AccessoryRequestDTO;
import com.gustavobatista.autoconfig.dto.AccessoryResponseDTO;

public interface AccessoryService {

    public AccessoryResponseDTO createAccessory(AccessoryRequestDTO dto);

    public AccessoryResponseDTO updateAccessory(Long id, AccessoryRequestDTO dto);

    public void deleteAccessory(Long id);

    public List<AccessoryResponseDTO> findAllAccessories();

    public AccessoryResponseDTO findAccessoryById(Long id);
}