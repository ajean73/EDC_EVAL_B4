package com.gamesUP.gamesUP.repository;

import com.gamesUP.gamesUP.domain.Category;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
}
