package com.gamesUP.gamesUP.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderLineResponse(
    UUID id,
    UUID gameId,
    Integer quantity,
    BigDecimal unitPrice
) {
}
