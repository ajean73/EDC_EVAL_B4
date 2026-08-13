package com.gamesUP.gamesUP.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gamesUP.gamesUP.domain.Category;
import com.gamesUP.gamesUP.repository.CategoryRepository;
import com.gamesUP.gamesUP.service.ResourceNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    private CategoryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CategoryServiceImpl(categoryRepository);
    }

    @Test
    void findAllReturnsRepositoryValues() {
        Category c1 = category("C1", "D1");
        Category c2 = category("C2", "D2");
        when(categoryRepository.findAll()).thenReturn(List.of(c1, c2));

        List<Category> result = service.findAll();

        assertEquals(2, result.size());
        assertSame(c1, result.get(0));
        verify(categoryRepository).findAll();
    }

    @Test
    void findByIdReturnsEntityWhenPresent() {
        UUID id = UUID.randomUUID();
        Category existing = category("Strategie", "jeux experts");
        existing.setId(id);
        when(categoryRepository.findById(id)).thenReturn(Optional.of(existing));

        Category result = service.findById(id);

        assertSame(existing, result);
    }

    @Test
    void findByIdThrowsWhenMissing() {
        UUID id = UUID.randomUUID();
        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(id));
    }

    @Test
    void createSavesAndReturnsEntity() {
        Category toCreate = category("Famille", "accessible");
        when(categoryRepository.save(toCreate)).thenReturn(toCreate);

        Category result = service.create(toCreate);

        assertSame(toCreate, result);
        verify(categoryRepository).save(toCreate);
    }

    @Test
    void updateCopiesFieldsAndSaves() {
        UUID id = UUID.randomUUID();
        Category existing = category("Old", "Old desc");
        existing.setId(id);
        Category incoming = category("New", "New desc");

        when(categoryRepository.findById(id)).thenReturn(Optional.of(existing));
        when(categoryRepository.save(existing)).thenReturn(existing);

        Category result = service.update(id, incoming);

        assertEquals("New", result.getName());
        assertEquals("New desc", result.getDescription());
        verify(categoryRepository).save(existing);
    }

    @Test
    void deleteRemovesExistingEntity() {
        UUID id = UUID.randomUUID();
        Category existing = category("ToDelete", "desc");
        existing.setId(id);
        when(categoryRepository.findById(id)).thenReturn(Optional.of(existing));

        service.delete(id);

        verify(categoryRepository).delete(existing);
    }

    private Category category(String name, String description) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        return category;
    }
}