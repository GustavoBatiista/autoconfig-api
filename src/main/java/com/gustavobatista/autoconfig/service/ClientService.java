package com.gustavobatista.autoconfig.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.gustavobatista.autoconfig.dto.ClientRequestDTO;
import com.gustavobatista.autoconfig.dto.ClientResponseDTO;

public interface ClientService {

    public ClientResponseDTO createClient(ClientRequestDTO dto);

    public ClientResponseDTO updateClient(Long id, ClientRequestDTO dto);

    public void deleteClient(Long id);

    public Page<ClientResponseDTO> findAllClients(Pageable pageable);

    public ClientResponseDTO findClientById(Long id);
}