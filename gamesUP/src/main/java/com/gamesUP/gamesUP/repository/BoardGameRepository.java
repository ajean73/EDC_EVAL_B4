package com.gamesUP.gamesUP.repository;

import com.gamesUP.gamesUP.domain.BoardGame;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardGameRepository extends JpaRepository<BoardGame, UUID> {
    List<BoardGame> findByTitleContainingIgnoreCase(String title);
}
