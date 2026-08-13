package com.gamesUP.gamesUP.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gamesUP.gamesUP.domain.Author;
import com.gamesUP.gamesUP.repository.AuthorRepository;
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
class AuthorServiceImplTest {

    @Mock
    private AuthorRepository authorRepository;

    private AuthorServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AuthorServiceImpl(authorRepository);
    }

    @Test
    void findAllReturnsRepositoryValues() {
        Author a1 = author("A1");
        Author a2 = author("A2");
        when(authorRepository.findAll()).thenReturn(List.of(a1, a2));

        List<Author> result = service.findAll();

        assertEquals(2, result.size());
        assertSame(a1, result.get(0));
        verify(authorRepository).findAll();
    }

    @Test
    void findByIdReturnsEntityWhenPresent() {
        UUID id = UUID.randomUUID();
        Author existing = author("Bruno");
        existing.setId(id);
        when(authorRepository.findById(id)).thenReturn(Optional.of(existing));

        Author result = service.findById(id);

        assertSame(existing, result);
    }

    @Test
    void findByIdThrowsWhenMissing() {
        UUID id = UUID.randomUUID();
        when(authorRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(id));
    }

    @Test
    void createSavesAndReturnsEntity() {
        Author toCreate = author("Nouveau");
        when(authorRepository.save(toCreate)).thenReturn(toCreate);

        Author result = service.create(toCreate);

        assertSame(toCreate, result);
        verify(authorRepository).save(toCreate);
    }

    @Test
    void updateCopiesNameAndSaves() {
        UUID id = UUID.randomUUID();
        Author existing = author("Old");
        existing.setId(id);
        Author incoming = author("New");

        when(authorRepository.findById(id)).thenReturn(Optional.of(existing));
        when(authorRepository.save(existing)).thenReturn(existing);

        Author result = service.update(id, incoming);

        assertEquals("New", result.getName());
        verify(authorRepository).save(existing);
    }

    @Test
    void deleteRemovesExistingEntity() {
        UUID id = UUID.randomUUID();
        Author existing = author("ToDelete");
        existing.setId(id);
        when(authorRepository.findById(id)).thenReturn(Optional.of(existing));

        service.delete(id);

        verify(authorRepository).delete(existing);
    }

    private Author author(String name) {
        Author author = new Author();
        author.setName(name);
        return author;
    }
}