package com.gamesUP.gamesUP.web;

import com.gamesUP.gamesUP.domain.BoardGame;
import com.gamesUP.gamesUP.domain.OrderLine;
import com.gamesUP.gamesUP.domain.PurchaseOrder;
import com.gamesUP.gamesUP.domain.UserAccount;
import com.gamesUP.gamesUP.service.PurchaseOrderService;
import com.gamesUP.gamesUP.web.dto.OrderLineRequest;
import com.gamesUP.gamesUP.web.dto.OrderLineResponse;
import com.gamesUP.gamesUP.web.dto.PurchaseOrderRequest;
import com.gamesUP.gamesUP.web.dto.PurchaseOrderResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/commerce/orders")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    public PurchaseOrderController(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    @GetMapping
    public List<PurchaseOrderResponse> findAll() {
        return purchaseOrderService.findAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public PurchaseOrderResponse findById(@PathVariable UUID id) {
        return toResponse(purchaseOrderService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseOrderResponse create(@RequestBody PurchaseOrderRequest request) {
        PurchaseOrder created = purchaseOrderService.create(toEntity(request));
        return toResponse(created);
    }

    @PutMapping("/{id}")
    public PurchaseOrderResponse update(@PathVariable UUID id, @RequestBody PurchaseOrderRequest request) {
        PurchaseOrder updated = purchaseOrderService.update(id, toEntity(request));
        return toResponse(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        purchaseOrderService.delete(id);
    }

    private PurchaseOrder toEntity(PurchaseOrderRequest request) {
        PurchaseOrder order = new PurchaseOrder();
        order.setOrderedAt(request.orderedAt());
        order.setStatus(request.status());
        order.setShippingAddress(request.shippingAddress());

        UserAccount user = new UserAccount();
        user.setId(request.userId());
        order.setUser(user);

        List<OrderLine> lines = new ArrayList<>();
        if (request.lines() != null) {
            for (OrderLineRequest lineRequest : request.lines()) {
                OrderLine line = new OrderLine();
                line.setQuantity(lineRequest.quantity());
                line.setUnitPrice(lineRequest.unitPrice());

                BoardGame game = new BoardGame();
                game.setId(lineRequest.gameId());
                line.setGame(game);
                line.setOrder(order);
                lines.add(line);
            }
        }
        order.setLines(lines);

        return order;
    }

    private PurchaseOrderResponse toResponse(PurchaseOrder order) {
        List<OrderLineResponse> lineResponses = order.getLines().stream()
            .map(line -> new OrderLineResponse(
                line.getId(),
                line.getGame().getId(),
                line.getQuantity(),
                line.getUnitPrice()
            ))
            .toList();

        return new PurchaseOrderResponse(
            order.getId(),
            order.getUser().getId(),
            order.getOrderedAt(),
            order.getStatus(),
            order.getTotalAmount(),
            order.getShippingAddress(),
            lineResponses,
            order.getCreatedAt(),
            order.getUpdatedAt()
        );
    }
}
