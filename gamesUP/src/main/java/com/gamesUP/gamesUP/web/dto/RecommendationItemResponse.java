package com.gamesUP.gamesUP.web.dto;

import java.util.UUID;

public record RecommendationItemResponse(
    UUID gameId,
    String title,
    double score,
    String reason
) {
}
