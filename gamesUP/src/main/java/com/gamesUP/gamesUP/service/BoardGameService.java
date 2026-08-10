package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.domain.BoardGame;
import java.util.List;
import java.util.UUID;

public interface BoardGameService {
    List<BoardGame> findAll();

    BoardGame findById(UUID id);

    BoardGame create(BoardGame boardGame);

    BoardGame update(UUID id, BoardGame boardGame);

    void delete(UUID id);

    List<BoardGame> searchByTitle(String title);
}
