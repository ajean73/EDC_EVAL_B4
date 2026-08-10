package com.gamesUP.gamesUP.web.dto;

import com.gamesUP.gamesUP.domain.UserRole;

public record UserAccountRequest(
    String firstName,
    String lastName,
    String email,
    String passwordHash,
    UserRole role
) {
}
