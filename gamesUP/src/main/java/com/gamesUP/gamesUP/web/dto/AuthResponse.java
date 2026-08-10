package com.gamesUP.gamesUP.web.dto;

import com.gamesUP.gamesUP.domain.UserRole;
import java.util.UUID;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    long accessTokenExpiresInMs,
    UUID userId,
    String email,
    UserRole role
) {
}
