package com.gamesUP.gamesUP.web.dto;

public record RecommendationTrainResponse(
    boolean trained,
    int gamesCount,
    int interactionsCount,
    String trainedAt
) {
}
