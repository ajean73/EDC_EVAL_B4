package com.gamesUP.gamesUP.web.dto;

import java.util.List;
import java.util.UUID;

public record UserRecommendationResponse(
    UUID userId,
    boolean modelTrained,
    List<RecommendationItemResponse> recommendations
) {
}
