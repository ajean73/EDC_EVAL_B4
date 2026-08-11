package com.gamesUP.gamesUP.web;

import com.gamesUP.gamesUP.service.recommendation.RecommendationService;
import com.gamesUP.gamesUP.web.dto.RecommendationItemResponse;
import com.gamesUP.gamesUP.web.dto.RecommendationTrainRequest;
import com.gamesUP.gamesUP.web.dto.RecommendationTrainResponse;
import com.gamesUP.gamesUP.web.dto.UserRecommendationResponse;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/commerce/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping("/train")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public RecommendationTrainResponse trainModel(@RequestBody(required = false) RecommendationTrainRequest request) {
        int nNeighbors = request != null && request.nNeighbors() > 0 ? request.nNeighbors() : 5;
        RecommendationService.TrainJobResult result = recommendationService.trainModel(nNeighbors);
        return new RecommendationTrainResponse(
            result.trained(),
            result.gamesCount(),
            result.interactionsCount(),
            result.trainedAt()
        );
    }

    @GetMapping("/users/{userId}")
    public UserRecommendationResponse recommendForUser(
        @PathVariable UUID userId,
        @RequestParam(name = "topK", defaultValue = "5") int topK
    ) {
        RecommendationService.UserRecommendationResult result = recommendationService.recommendForUser(userId, topK);
        return new UserRecommendationResponse(
            result.userId(),
            result.modelTrained(),
            result.recommendations().stream()
                .map(game -> new RecommendationItemResponse(game.gameId(), game.title(), game.score(), game.reason()))
                .toList()
        );
    }
}
