package com.gustavobatista.autoconfig.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.gustavobatista.autoconfig.dto.UserRequestDTO;
import com.gustavobatista.autoconfig.dto.UserResponseDTO;

public interface UserService {

    public UserResponseDTO createUser(UserRequestDTO userRequestDTO);

    public UserResponseDTO updateUser(Long id, UserRequestDTO userRequestDTO);

    public void deleteUser(Long id);

    public Page<UserResponseDTO> findAll(Pageable pageable);

    public UserResponseDTO findById(Long id);
}