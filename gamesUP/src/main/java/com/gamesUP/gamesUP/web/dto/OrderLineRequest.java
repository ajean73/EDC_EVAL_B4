package com.gamesUP.gamesUP.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderLineRequest(
    UUID gameId,
    Integer quantity,
    BigDecimal unitPrice
) {
}
