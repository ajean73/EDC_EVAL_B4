package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.domain.PurchaseOrder;
import java.util.List;
import java.util.UUID;

public interface PurchaseOrderService {
    List<PurchaseOrder> findAll();

    PurchaseOrder findById(UUID id);

    PurchaseOrder create(PurchaseOrder purchaseOrder);

    PurchaseOrder update(UUID id, PurchaseOrder purchaseOrder);

    void delete(UUID id);
}
