package com.gamesUP.gamesUP.service.recommendation;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public final class RecommendationClientPayloads {

    private RecommendationClientPayloads() {
    }

    public record CatalogItem(
        @JsonProperty("game_id") String gameId,
        String title,
        String publisher,
        List<String> categories,
        List<String> authors,
        double price,
        @JsonProperty("average_rating") double averageRating
    ) {
    }

    public record Interaction(@JsonProperty("user_id") String userId, @JsonProperty("game_id") String gameId, double rating) {
    }

    public record TrainRequest(
        List<CatalogItem> catalog,
        List<Interaction> interactions,
        @JsonProperty("n_neighbors") int nNeighbors
    ) {
    }

    public record TrainResponse(
        boolean trained,
        @JsonProperty("games_count") int gamesCount,
        @JsonProperty("interactions_count") int interactionsCount,
        @JsonProperty("trained_at") String trainedAt
    ) {
    }

    public record UserPurchase(@JsonProperty("game_id") String gameId, double rating) {
    }

    public record UserData(@JsonProperty("user_id") String userId, List<UserPurchase> purchases) {
    }

    public record PredictRequest(@JsonProperty("user_data") UserData userData, @JsonProperty("top_k") int topK) {
    }

    public record RecommendationItem(@JsonProperty("game_id") String gameId, double score, String reason) {
    }

    public record PredictResponse(
        @JsonProperty("user_id") String userId,
        @JsonProperty("model_trained") boolean modelTrained,
        List<RecommendationItem> recommendations
    ) {
    }
}
