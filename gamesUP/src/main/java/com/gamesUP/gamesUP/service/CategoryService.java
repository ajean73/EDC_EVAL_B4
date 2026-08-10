package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.domain.Category;
import java.util.List;
import java.util.UUID;

public interface CategoryService {
    List<Category> findAll();

    Category findById(UUID id);

    Category create(Category category);

    Category update(UUID id, Category category);

    void delete(UUID id);
}
