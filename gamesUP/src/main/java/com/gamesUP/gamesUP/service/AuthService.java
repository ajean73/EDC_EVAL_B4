package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.web.dto.AuthLoginRequest;
import com.gamesUP.gamesUP.web.dto.AuthRegisterRequest;
import com.gamesUP.gamesUP.web.dto.AuthResponse;
import com.gamesUP.gamesUP.web.dto.RefreshTokenRequest;

public interface AuthService {
    AuthResponse register(AuthRegisterRequest request);

    AuthResponse login(AuthLoginRequest request);

    AuthResponse refresh(RefreshTokenRequest request);

    void logout(RefreshTokenRequest request);
}
