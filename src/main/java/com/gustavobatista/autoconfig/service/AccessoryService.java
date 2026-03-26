package com.gustavobatista.autoconfig.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.gustavobatista.autoconfig.dto.AccessoryRequestDTO;
import com.gustavobatista.autoconfig.dto.AccessoryResponseDTO;

public interface AccessoryService {

    public AccessoryResponseDTO createAccessory(AccessoryRequestDTO dto);

    public AccessoryResponseDTO updateAccessory(Long id, AccessoryRequestDTO dto);

    public void deleteAccessory(Long id);

    public Page<AccessoryResponseDTO> findAllAccessories(Pageable pageable);

    public AccessoryResponseDTO findAccessoryById(Long id);
}