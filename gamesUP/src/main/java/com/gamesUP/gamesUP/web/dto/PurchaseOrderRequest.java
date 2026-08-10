package com.gamesUP.gamesUP.web.dto;

import com.gamesUP.gamesUP.domain.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PurchaseOrderRequest(
    UUID userId,
    LocalDateTime orderedAt,
    OrderStatus status,
    String shippingAddress,
    List<OrderLineRequest> lines
) {
}
