package com.gamesUP.gamesUP.service.impl.recommendation;

import com.gamesUP.gamesUP.domain.Author;
import com.gamesUP.gamesUP.domain.BoardGame;
import com.gamesUP.gamesUP.domain.Category;
import com.gamesUP.gamesUP.domain.OrderLine;
import com.gamesUP.gamesUP.domain.PurchaseOrder;
import com.gamesUP.gamesUP.domain.UserAccount;
import com.gamesUP.gamesUP.repository.BoardGameRepository;
import com.gamesUP.gamesUP.repository.PurchaseOrderRepository;
import com.gamesUP.gamesUP.repository.UserAccountRepository;
import com.gamesUP.gamesUP.service.ResourceNotFoundException;
import com.gamesUP.gamesUP.service.recommendation.RecommendationClientPayloads;
import com.gamesUP.gamesUP.service.recommendation.RecommendationService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@Transactional(readOnly = true)
public class RecommendationServiceImpl implements RecommendationService {

    private static final double DEFAULT_RATING = 4.0;
    private static final String TOP_SALES_FALLBACK_NO_HISTORY = "top_sales_fallback_no_history";
    private static final String TOP_SALES_FALLBACK_ML_UNAVAILABLE = "top_sales_fallback_ml_unavailable";
    private static final String TOP_RATED_FALLBACK_NO_SALES = "top_rated_fallback_no_sales";

    private final BoardGameRepository boardGameRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final UserAccountRepository userAccountRepository;
    private final RestTemplate restTemplate;
    private final String pythonApiBaseUrl;

    public RecommendationServiceImpl(
        BoardGameRepository boardGameRepository,
        PurchaseOrderRepository purchaseOrderRepository,
        UserAccountRepository userAccountRepository,
        RestTemplate restTemplate,
        @Value("${recommendation.python.base-url:http://localhost:8001}") String pythonApiBaseUrl
    ) {
        this.boardGameRepository = boardGameRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.userAccountRepository = userAccountRepository;
        this.restTemplate = restTemplate;
        this.pythonApiBaseUrl = pythonApiBaseUrl;
    }

    @Override
    @Transactional
    public TrainJobResult trainModel(int nNeighbors) {
        List<BoardGame> games = boardGameRepository.findAll();
        if (games.size() < 2) {
            throw new IllegalArgumentException("At least 2 board games are required to train recommendations");
        }

        List<RecommendationClientPayloads.CatalogItem> catalogItems = games.stream()
            .map(this::toCatalogItem)
            .toList();

        List<RecommendationClientPayloads.Interaction> interactions = purchaseOrderRepository.findAll().stream()
            .flatMap(order -> order.getLines().stream().map(line -> toInteraction(order, line)))
            .toList();

        RecommendationClientPayloads.TrainRequest request = new RecommendationClientPayloads.TrainRequest(
            catalogItems,
            interactions,
            Math.max(2, nNeighbors)
        );

        RecommendationClientPayloads.TrainResponse response;
        try {
            response = restTemplate.postForObject(
                pythonApiBaseUrl + "/recommendations/train",
                request,
                RecommendationClientPayloads.TrainResponse.class
            );
        } catch (RestClientException ex) {
            throw new IllegalStateException("Unable to train recommendation model on Python API", ex);
        }

        if (response == null) {
            throw new IllegalStateException("Python recommendation API returned an empty train response");
        }

        return new TrainJobResult(
            response.trained(),
            response.gamesCount(),
            response.interactionsCount(),
            response.trainedAt()
        );
    }

    @Override
    public UserRecommendationResult recommendForUser(UUID userId, int topK) {
        UserAccount user = userAccountRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        int safeTopK = Math.max(1, topK);
        List<PurchaseOrder> orders = purchaseOrderRepository.findByUser(user);
        if (orders.isEmpty()) {
            return buildTopSalesFallback(userId, safeTopK, Set.of(), TOP_SALES_FALLBACK_NO_HISTORY);
        }

        Map<UUID, Double> purchaseRatingsByGameId = new HashMap<>();
        Set<UUID> purchasedGameIds = new HashSet<>();
        for (PurchaseOrder order : orders) {
            for (OrderLine line : order.getLines()) {
                UUID gameId = line.getGame().getId();
                purchasedGameIds.add(gameId);
                double rating = deriveRating(line.getGame().getAverageRating());
                purchaseRatingsByGameId.merge(gameId, rating, Math::max);
            }
        }

        List<RecommendationClientPayloads.UserPurchase> purchases = purchaseRatingsByGameId.entrySet().stream()
            .map(entry -> new RecommendationClientPayloads.UserPurchase(entry.getKey().toString(), entry.getValue()))
            .toList();

        RecommendationClientPayloads.PredictRequest request = new RecommendationClientPayloads.PredictRequest(
            new RecommendationClientPayloads.UserData(userId.toString(), purchases),
            safeTopK
        );

        RecommendationClientPayloads.PredictResponse response;
        try {
            response = restTemplate.postForObject(
                pythonApiBaseUrl + "/recommendations/predict",
                request,
                RecommendationClientPayloads.PredictResponse.class
            );
        } catch (RestClientException ex) {
            // Si le service ML est indisponible, on bascule sur le fallback métier.
            return buildTopSalesFallback(userId, safeTopK, purchasedGameIds, TOP_SALES_FALLBACK_ML_UNAVAILABLE);
        }

        if (response == null || response.recommendations() == null || !response.modelTrained()) {
            return buildTopSalesFallback(userId, safeTopK, purchasedGameIds, TOP_SALES_FALLBACK_ML_UNAVAILABLE);
        }

        Map<UUID, BoardGame> gamesById = new HashMap<>();
        for (BoardGame game : boardGameRepository.findAll()) {
            gamesById.put(game.getId(), game);
        }

        List<RecommendedGame> recommendations = new ArrayList<>();
        for (RecommendationClientPayloads.RecommendationItem item : response.recommendations()) {
            UUID gameId;
            try {
                gameId = UUID.fromString(item.gameId());
            } catch (IllegalArgumentException ex) {
                continue;
            }

            BoardGame game = gamesById.get(gameId);
            if (game == null) {
                continue;
            }

            recommendations.add(
                new RecommendedGame(
                    game.getId(),
                    game.getTitle(),
                    item.score(),
                    item.reason()
                )
            );
        }

        if (recommendations.isEmpty()) {
            return buildTopSalesFallback(userId, safeTopK, purchasedGameIds, TOP_SALES_FALLBACK_ML_UNAVAILABLE);
        }

        recommendations.sort(Comparator.comparingDouble(RecommendedGame::score).reversed());

        return new UserRecommendationResult(
            userId,
            response.modelTrained(),
            recommendations
        );
    }

    private UserRecommendationResult buildTopSalesFallback(UUID userId, int topK, Set<UUID> excludedGameIds, String reason) {
        // Fallback principal: on priorise les jeux les plus vendus.
        Map<UUID, Integer> soldUnitsByGameId = new HashMap<>();
        for (PurchaseOrder order : purchaseOrderRepository.findAll()) {
            for (OrderLine line : order.getLines()) {
                if (line.getGame() == null || line.getGame().getId() == null) {
                    continue;
                }
                int units = line.getQuantity() != null ? line.getQuantity() : 0;
                soldUnitsByGameId.merge(line.getGame().getId(), units, Integer::sum);
            }
        }

        Map<UUID, BoardGame> gamesById = new HashMap<>();
        for (BoardGame game : boardGameRepository.findAll()) {
            gamesById.put(game.getId(), game);
        }

        List<RecommendedGame> recommendations = soldUnitsByGameId.entrySet().stream()
            .filter(entry -> !excludedGameIds.contains(entry.getKey()))
            .filter(entry -> gamesById.containsKey(entry.getKey()))
            .sorted((left, right) -> Integer.compare(right.getValue(), left.getValue()))
            .limit(topK)
            .map(entry -> {
                BoardGame game = gamesById.get(entry.getKey());
                return new RecommendedGame(game.getId(), game.getTitle(), entry.getValue(), reason);
            })
            .toList();

        if (recommendations.size() < topK) {
            Set<UUID> alreadyRecommendedIds = recommendations.stream()
                .map(RecommendedGame::gameId)
                .collect(java.util.stream.Collectors.toSet());

            List<RecommendedGame> topRated = gamesById.values().stream()
                .filter(game -> !excludedGameIds.contains(game.getId()))
                .filter(game -> !alreadyRecommendedIds.contains(game.getId()))
                .sorted(
                    Comparator
                        .comparingDouble((BoardGame game) -> deriveRating(game.getAverageRating()))
                        .reversed()
                )
                .limit(topK - recommendations.size())
                .map(game -> new RecommendedGame(game.getId(), game.getTitle(), deriveRating(game.getAverageRating()), TOP_RATED_FALLBACK_NO_SALES))
                .toList();

            List<RecommendedGame> merged = new ArrayList<>(recommendations);
            merged.addAll(topRated);
            recommendations = merged;
        }

        return new UserRecommendationResult(userId, false, recommendations);
    }

    private RecommendationClientPayloads.CatalogItem toCatalogItem(BoardGame game) {
        String publisherName = game.getPublisher() != null ? game.getPublisher().getName() : "unknown";

        List<String> categoryNames = game.getCategories().stream().map(Category::getName).sorted().toList();
        List<String> authorNames = game.getAuthors().stream().map(Author::getName).sorted().toList();

        return new RecommendationClientPayloads.CatalogItem(
            game.getId().toString(),
            game.getTitle(),
            publisherName,
            categoryNames,
            authorNames,
            toDouble(game.getPrice()),
            deriveRating(game.getAverageRating())
        );
    }

    private RecommendationClientPayloads.Interaction toInteraction(PurchaseOrder order, OrderLine line) {
        return new RecommendationClientPayloads.Interaction(
            order.getUser().getId().toString(),
            line.getGame().getId().toString(),
            deriveRating(line.getGame().getAverageRating())
        );
    }

    private double deriveRating(BigDecimal averageRating) {
        return averageRating != null ? averageRating.doubleValue() : DEFAULT_RATING;
    }

    private double toDouble(BigDecimal value) {
        return value != null ? value.doubleValue() : 0.0;
    }
}
