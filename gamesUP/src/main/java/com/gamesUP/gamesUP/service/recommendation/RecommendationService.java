package com.gamesUP.gamesUP.service.recommendation;

import java.util.List;
import java.util.UUID;

public interface RecommendationService {
    TrainJobResult trainModel(int nNeighbors);

    UserRecommendationResult recommendForUser(UUID userId, int topK);

    record TrainJobResult(boolean trained, int gamesCount, int interactionsCount, String trainedAt) {
    }

    record RecommendedGame(UUID gameId, String title, double score, String reason) {
    }

    record UserRecommendationResult(UUID userId, boolean modelTrained, List<RecommendedGame> recommendations) {
    }
}
