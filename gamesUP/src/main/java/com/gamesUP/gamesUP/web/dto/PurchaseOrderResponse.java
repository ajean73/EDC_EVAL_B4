package com.gamesUP.gamesUP.web.dto;

import com.gamesUP.gamesUP.domain.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PurchaseOrderResponse(
    UUID id,
    UUID userId,
    LocalDateTime orderedAt,
    OrderStatus status,
    BigDecimal totalAmount,
    String shippingAddress,
    List<OrderLineResponse> lines,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
