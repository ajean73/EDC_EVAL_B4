package com.gamesUP.gamesUP.repository;

import com.gamesUP.gamesUP.domain.Wishlist;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishlistRepository extends JpaRepository<Wishlist, UUID> {
}
