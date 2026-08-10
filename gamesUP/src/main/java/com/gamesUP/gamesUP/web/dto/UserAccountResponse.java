package com.gamesUP.gamesUP.web.dto;

import com.gamesUP.gamesUP.domain.UserRole;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserAccountResponse(
    UUID id,
    String firstName,
    String lastName,
    String email,
    UserRole role,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
