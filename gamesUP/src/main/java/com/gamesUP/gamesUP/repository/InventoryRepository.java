package com.gamesUP.gamesUP.repository;

import com.gamesUP.gamesUP.domain.Inventory;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<Inventory, UUID> {
}
