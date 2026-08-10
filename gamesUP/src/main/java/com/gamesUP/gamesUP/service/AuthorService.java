package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.domain.Author;
import java.util.List;
import java.util.UUID;

public interface AuthorService {
    List<Author> findAll();

    Author findById(UUID id);

    Author create(Author author);

    Author update(UUID id, Author author);

    void delete(UUID id);
}
