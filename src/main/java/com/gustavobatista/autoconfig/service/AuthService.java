package com.gustavobatista.autoconfig.service;

import com.gustavobatista.autoconfig.dto.AuthMeResponseDTO;
import com.gustavobatista.autoconfig.dto.AuthRequestDTO;
import com.gustavobatista.autoconfig.dto.AuthResponseDTO;

public interface AuthService {

    AuthResponseDTO login(AuthRequestDTO request);

    AuthMeResponseDTO getCurrentUser();
}
