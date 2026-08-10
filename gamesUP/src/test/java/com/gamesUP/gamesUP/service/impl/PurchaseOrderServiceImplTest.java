package com.gamesUP.gamesUP.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.gamesUP.gamesUP.domain.BoardGame;
import com.gamesUP.gamesUP.domain.OrderLine;
import com.gamesUP.gamesUP.domain.OrderStatus;
import com.gamesUP.gamesUP.domain.PurchaseOrder;
import com.gamesUP.gamesUP.domain.UserAccount;
import com.gamesUP.gamesUP.repository.BoardGameRepository;
import com.gamesUP.gamesUP.repository.PurchaseOrderRepository;
import com.gamesUP.gamesUP.repository.UserAccountRepository;
import com.gamesUP.gamesUP.service.ResourceNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderServiceImplTest {

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private BoardGameRepository boardGameRepository;

    @InjectMocks
    private PurchaseOrderServiceImpl service;

    @Test
    void createSetsDefaultsAndComputesTotal() {
        UUID userId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();

        UserAccount userRef = new UserAccount();
        userRef.setId(userId);

        BoardGame gameRef = new BoardGame();
        gameRef.setId(gameId);

        UserAccount user = new UserAccount();
        user.setId(userId);

        BoardGame game = new BoardGame();
        game.setId(gameId);

        OrderLine line = new OrderLine();
        line.setGame(gameRef);
        line.setQuantity(2);
        line.setUnitPrice(new BigDecimal("12.50"));

        PurchaseOrder source = new PurchaseOrder();
        source.setShippingAddress("12 rue de la Paix");
        source.setUser(userRef);
        source.setLines(List.of(line));

        when(userAccountRepository.findById(userId)).thenReturn(Optional.of(user));
        when(boardGameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PurchaseOrder saved = service.create(source);

        assertNotNull(saved.getOrderedAt());
        assertEquals(OrderStatus.PENDING, saved.getStatus());
        assertEquals(new BigDecimal("25.00"), saved.getTotalAmount());
        assertEquals(1, saved.getLines().size());
    }

    @Test
    void createFailsWhenUserMissing() {
        PurchaseOrder source = new PurchaseOrder();

        assertThrows(ResourceNotFoundException.class, () -> service.create(source));
    }

    @Test
    void createFailsWhenGameMissingInLine() {
        UUID userId = UUID.randomUUID();

        UserAccount userRef = new UserAccount();
        userRef.setId(userId);

        PurchaseOrder source = new PurchaseOrder();
        source.setUser(userRef);

        OrderLine invalidLine = new OrderLine();
        source.setLines(List.of(invalidLine));

        UserAccount user = new UserAccount();
        user.setId(userId);

        when(userAccountRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(ResourceNotFoundException.class, () -> service.create(source));
    }
}
