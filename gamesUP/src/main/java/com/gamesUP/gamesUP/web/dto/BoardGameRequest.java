package com.gamesUP.gamesUP.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record BoardGameRequest(
    String title,
    String description,
    BigDecimal price,
    BigDecimal averageRating,
    LocalDate releaseDate,
    UUID publisherId,
    Set<UUID> authorIds,
    Set<UUID> categoryIds
) {
}
