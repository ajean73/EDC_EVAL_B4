package com.gamesUP.gamesUP.repository;

import com.gamesUP.gamesUP.domain.PurchaseOrder;
import com.gamesUP.gamesUP.domain.UserAccount;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, UUID> {
    List<PurchaseOrder> findByUser(UserAccount user);
}
