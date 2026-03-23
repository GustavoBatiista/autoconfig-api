package com.gustavobatista.autoconfig.service;

import java.util.List;

import com.gustavobatista.autoconfig.dto.UserRequestDTO;
import com.gustavobatista.autoconfig.dto.UserResponseDTO;

public interface UserService {

    public UserResponseDTO createUser(UserRequestDTO userRequestDTO);

    public UserResponseDTO updateUser(Long id, UserRequestDTO userRequestDTO);

    public void deleteUser(Long id);

    public List<UserResponseDTO> findAll();

    public UserResponseDTO findById(Long id);
}