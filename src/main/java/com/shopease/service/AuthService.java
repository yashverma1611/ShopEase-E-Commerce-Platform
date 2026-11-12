package com.shopease.service;

import com.shopease.dto.AuthRequestDTO;
import com.shopease.dto.AuthResponseDTO;

public interface AuthService {
    AuthResponseDTO register(AuthRequestDTO request);
    AuthResponseDTO login(AuthRequestDTO request);
}
