package com.gamesUP.gamesUP.service.impl;

import com.gamesUP.gamesUP.domain.BoardGame;
import com.gamesUP.gamesUP.domain.OrderLine;
import com.gamesUP.gamesUP.domain.OrderStatus;
import com.gamesUP.gamesUP.domain.PurchaseOrder;
import com.gamesUP.gamesUP.domain.UserAccount;
import com.gamesUP.gamesUP.repository.BoardGameRepository;
import com.gamesUP.gamesUP.repository.PurchaseOrderRepository;
import com.gamesUP.gamesUP.repository.UserAccountRepository;
import com.gamesUP.gamesUP.service.PurchaseOrderService;
import com.gamesUP.gamesUP.service.ResourceNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final UserAccountRepository userAccountRepository;
    private final BoardGameRepository boardGameRepository;

    public PurchaseOrderServiceImpl(
        PurchaseOrderRepository purchaseOrderRepository,
        UserAccountRepository userAccountRepository,
        BoardGameRepository boardGameRepository
    ) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.userAccountRepository = userAccountRepository;
        this.boardGameRepository = boardGameRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrder> findAll() {
        return purchaseOrderRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseOrder findById(UUID id) {
        return purchaseOrderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
    }

    @Override
    public PurchaseOrder create(PurchaseOrder purchaseOrder) {
        PurchaseOrder entity = new PurchaseOrder();
        applyUpdatableFields(entity, purchaseOrder);
        return purchaseOrderRepository.save(entity);
    }

    @Override
    public PurchaseOrder update(UUID id, PurchaseOrder purchaseOrder) {
        PurchaseOrder existing = findById(id);
        applyUpdatableFields(existing, purchaseOrder);
        return purchaseOrderRepository.save(existing);
    }

    @Override
    public void delete(UUID id) {
        PurchaseOrder existing = findById(id);
        purchaseOrderRepository.delete(existing);
    }

    private void applyUpdatableFields(PurchaseOrder target, PurchaseOrder source) {
        target.setOrderedAt(source.getOrderedAt() != null ? source.getOrderedAt() : LocalDateTime.now());
        target.setStatus(source.getStatus() != null ? source.getStatus() : OrderStatus.PENDING);
        target.setShippingAddress(source.getShippingAddress());

        UserAccount user = resolveUser(source.getUser());
        target.setUser(user);

        List<OrderLine> sourceLines = source.getLines() == null ? List.of() : source.getLines();
        List<OrderLine> managedLines = new ArrayList<>();

        for (OrderLine line : sourceLines) {
            BoardGame game = resolveGame(line.getGame());
            OrderLine managedLine = new OrderLine();
            managedLine.setOrder(target);
            managedLine.setGame(game);
            managedLine.setQuantity(line.getQuantity());
            managedLine.setUnitPrice(line.getUnitPrice());
            managedLines.add(managedLine);
        }

        target.getLines().clear();
        target.getLines().addAll(managedLines);

        BigDecimal totalAmount = managedLines.stream()
            .map(line -> line.getUnitPrice().multiply(BigDecimal.valueOf(line.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        target.setTotalAmount(totalAmount);
    }

    private UserAccount resolveUser(UserAccount user) {
        if (user == null || user.getId() == null) {
            throw new ResourceNotFoundException("User id is required");
        }
        return userAccountRepository.findById(user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + user.getId()));
    }

    private BoardGame resolveGame(BoardGame game) {
        if (game == null || game.getId() == null) {
            throw new ResourceNotFoundException("Game id is required in order lines");
        }
        return boardGameRepository.findById(game.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Game not found: " + game.getId()));
    }
}
