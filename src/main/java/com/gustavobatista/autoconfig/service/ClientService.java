package com.gustavobatista.autoconfig.service;

import java.util.List;

import com.gustavobatista.autoconfig.dto.ClientRequestDTO;
import com.gustavobatista.autoconfig.dto.ClientResponseDTO;

public interface ClientService {

    public ClientResponseDTO createClient(ClientRequestDTO dto);

    public ClientResponseDTO updateClient(Long id, ClientRequestDTO dto);

    public void deleteClient(Long id);

    public List<ClientResponseDTO> findAllClients();

    public ClientResponseDTO findClientById(Long id);
}