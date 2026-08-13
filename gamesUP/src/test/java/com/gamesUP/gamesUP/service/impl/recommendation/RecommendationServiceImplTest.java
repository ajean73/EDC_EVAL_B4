package com.gamesUP.gamesUP.service.impl.recommendation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.gamesUP.gamesUP.domain.BoardGame;
import com.gamesUP.gamesUP.domain.OrderLine;
import com.gamesUP.gamesUP.domain.PurchaseOrder;
import com.gamesUP.gamesUP.domain.UserAccount;
import com.gamesUP.gamesUP.repository.BoardGameRepository;
import com.gamesUP.gamesUP.repository.PurchaseOrderRepository;
import com.gamesUP.gamesUP.repository.UserAccountRepository;
import com.gamesUP.gamesUP.service.recommendation.RecommendationClientPayloads;
import com.gamesUP.gamesUP.service.recommendation.RecommendationService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceImplTest {

    @Mock
    private BoardGameRepository boardGameRepository;

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private RestTemplate restTemplate;

    private RecommendationService service;

    @BeforeEach
    void setUp() {
        service = new RecommendationServiceImpl(
            boardGameRepository,
            purchaseOrderRepository,
            userAccountRepository,
            restTemplate,
            "http://python:8001"
        );
    }

    @Test
    void recommendForUserFallsBackToTopSalesWhenPythonIsUnavailable() {
        UUID userId = UUID.randomUUID();
        UserAccount user = new UserAccount();
        user.setId(userId);

        BoardGame purchasedGame = game("Dixit", new BigDecimal("4.3"));
        BoardGame topSeller = game("7 Wonders", new BigDecimal("4.6"));
        BoardGame secondSeller = game("Takenoko", new BigDecimal("4.1"));

        PurchaseOrder userOrder = orderWithLines(line(purchasedGame, 1));
        PurchaseOrder salesOrderA = orderWithLines(line(topSeller, 5));
        PurchaseOrder salesOrderB = orderWithLines(line(secondSeller, 3));

        when(userAccountRepository.findById(userId)).thenReturn(Optional.of(user));
        when(purchaseOrderRepository.findByUser(user)).thenReturn(List.of(userOrder));
        when(purchaseOrderRepository.findAll()).thenReturn(List.of(userOrder, salesOrderA, salesOrderB));
        when(boardGameRepository.findAll()).thenReturn(List.of(purchasedGame, topSeller, secondSeller));
        when(restTemplate.postForObject(any(String.class), any(), any(Class.class)))
            .thenThrow(new RestClientException("python unavailable"));

        RecommendationService.UserRecommendationResult result = service.recommendForUser(userId, 2);

        assertFalse(result.modelTrained());
        assertEquals(2, result.recommendations().size());
        assertEquals(topSeller.getId(), result.recommendations().get(0).gameId());
        assertEquals(secondSeller.getId(), result.recommendations().get(1).gameId());
        assertEquals("top_sales_fallback_ml_unavailable", result.recommendations().get(0).reason());
    }

    @Test
    void recommendForUserFallsBackToTopSalesWhenUserHasNoPurchaseHistory() {
        UUID userId = UUID.randomUUID();
        UserAccount user = new UserAccount();
        user.setId(userId);

        BoardGame topSeller = game("Les Aventuriers du Rail - Europe", new BigDecimal("4.5"));
        BoardGame secondSeller = game("Dixit", new BigDecimal("4.2"));

        PurchaseOrder salesOrderA = orderWithLines(line(topSeller, 7));
        PurchaseOrder salesOrderB = orderWithLines(line(secondSeller, 4));

        when(userAccountRepository.findById(userId)).thenReturn(Optional.of(user));
        when(purchaseOrderRepository.findByUser(user)).thenReturn(List.of());
        when(purchaseOrderRepository.findAll()).thenReturn(List.of(salesOrderA, salesOrderB));
        when(boardGameRepository.findAll()).thenReturn(List.of(topSeller, secondSeller));

        RecommendationService.UserRecommendationResult result = service.recommendForUser(userId, 2);

        assertFalse(result.modelTrained());
        assertEquals(2, result.recommendations().size());
        assertEquals(topSeller.getId(), result.recommendations().get(0).gameId());
        assertEquals("top_sales_fallback_no_history", result.recommendations().get(0).reason());
    }

    @Test
    void trainModelThrowsWhenCatalogHasLessThanTwoGames() {
        BoardGame onlyGame = game("Single Game", new BigDecimal("4.0"));
        when(boardGameRepository.findAll()).thenReturn(List.of(onlyGame));

        assertThrows(IllegalArgumentException.class, () -> service.trainModel(5));
    }

    @Test
    void recommendForUserFallsBackWhenPythonResponseIsNotTrained() {
        UUID userId = UUID.randomUUID();
        UserAccount user = new UserAccount();
        user.setId(userId);

        BoardGame purchasedGame = game("Dixit", new BigDecimal("4.3"));
        BoardGame topSeller = game("7 Wonders", new BigDecimal("4.6"));

        PurchaseOrder userOrder = orderWithLines(line(purchasedGame, 1));
        PurchaseOrder salesOrder = orderWithLines(line(topSeller, 6));

        when(userAccountRepository.findById(userId)).thenReturn(Optional.of(user));
        when(purchaseOrderRepository.findByUser(user)).thenReturn(List.of(userOrder));
        when(purchaseOrderRepository.findAll()).thenReturn(List.of(userOrder, salesOrder));
        when(boardGameRepository.findAll()).thenReturn(List.of(purchasedGame, topSeller));
        when(restTemplate.postForObject(any(String.class), any(), any(Class.class)))
            .thenReturn(new RecommendationClientPayloads.PredictResponse(userId.toString(), false, List.of()));

        RecommendationService.UserRecommendationResult result = service.recommendForUser(userId, 2);

        assertFalse(result.modelTrained());
        assertEquals(1, result.recommendations().size());
        assertEquals(topSeller.getId(), result.recommendations().get(0).gameId());
        assertEquals("top_sales_fallback_ml_unavailable", result.recommendations().get(0).reason());
    }

    @Test
    void recommendForUserFallsBackWhenPythonRecommendationsContainOnlyInvalidIds() {
        UUID userId = UUID.randomUUID();
        UserAccount user = new UserAccount();
        user.setId(userId);

        BoardGame purchasedGame = game("Dixit", new BigDecimal("4.3"));
        BoardGame topSeller = game("7 Wonders", new BigDecimal("4.6"));

        PurchaseOrder userOrder = orderWithLines(line(purchasedGame, 1));
        PurchaseOrder salesOrder = orderWithLines(line(topSeller, 6));

        RecommendationClientPayloads.RecommendationItem invalidItem =
            new RecommendationClientPayloads.RecommendationItem("not-a-uuid", 0.95, "ml");
        RecommendationClientPayloads.PredictResponse pythonResponse =
            new RecommendationClientPayloads.PredictResponse(userId.toString(), true, List.of(invalidItem));

        when(userAccountRepository.findById(userId)).thenReturn(Optional.of(user));
        when(purchaseOrderRepository.findByUser(user)).thenReturn(List.of(userOrder));
        when(purchaseOrderRepository.findAll()).thenReturn(List.of(userOrder, salesOrder));
        when(boardGameRepository.findAll()).thenReturn(List.of(purchasedGame, topSeller));
        when(restTemplate.postForObject(any(String.class), any(), any(Class.class))).thenReturn(pythonResponse);

        RecommendationService.UserRecommendationResult result = service.recommendForUser(userId, 2);

        assertFalse(result.modelTrained());
        assertEquals(1, result.recommendations().size());
        assertEquals(topSeller.getId(), result.recommendations().get(0).gameId());
        assertEquals("top_sales_fallback_ml_unavailable", result.recommendations().get(0).reason());
    }

    @Test
    void recommendForUserCompletesWithTopRatedWhenSalesFallbackIsInsufficient() {
        UUID userId = UUID.randomUUID();
        UserAccount user = new UserAccount();
        user.setId(userId);

        BoardGame topRatedA = game("Ark Nova", new BigDecimal("4.8"));
        BoardGame topRatedB = game("Brass", new BigDecimal("4.7"));

        when(userAccountRepository.findById(userId)).thenReturn(Optional.of(user));
        when(purchaseOrderRepository.findByUser(user)).thenReturn(List.of());
        when(purchaseOrderRepository.findAll()).thenReturn(List.of());
        when(boardGameRepository.findAll()).thenReturn(List.of(topRatedA, topRatedB));

        RecommendationService.UserRecommendationResult result = service.recommendForUser(userId, 2);

        assertFalse(result.modelTrained());
        assertEquals(2, result.recommendations().size());
        assertEquals("top_rated_fallback_no_sales", result.recommendations().get(0).reason());
        assertEquals("top_rated_fallback_no_sales", result.recommendations().get(1).reason());
    }

    private BoardGame game(String title, BigDecimal averageRating) {
        BoardGame game = new BoardGame();
        game.setId(UUID.randomUUID());
        game.setTitle(title);
        game.setAverageRating(averageRating);
        return game;
    }

    private PurchaseOrder orderWithLines(OrderLine... lines) {
        PurchaseOrder order = new PurchaseOrder();
        order.setLines(List.of(lines));
        return order;
    }

    private OrderLine line(BoardGame game, int quantity) {
        OrderLine line = new OrderLine();
        line.setGame(game);
        line.setQuantity(quantity);
        return line;
    }
}
