package com.gamesUP.gamesUP.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gamesUP.gamesUP.domain.Author;
import com.gamesUP.gamesUP.domain.BoardGame;
import com.gamesUP.gamesUP.domain.Category;
import com.gamesUP.gamesUP.domain.Publisher;
import com.gamesUP.gamesUP.repository.AuthorRepository;
import com.gamesUP.gamesUP.repository.BoardGameRepository;
import com.gamesUP.gamesUP.repository.CategoryRepository;
import com.gamesUP.gamesUP.repository.PublisherRepository;
import com.gamesUP.gamesUP.service.ResourceNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BoardGameServiceImplTest {

    @Mock
    private BoardGameRepository boardGameRepository;

    @Mock
    private PublisherRepository publisherRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private BoardGameServiceImpl service;

    @Test
    void searchByTitleBlankReturnsAll() {
        BoardGame game = new BoardGame();
        when(boardGameRepository.findAll()).thenReturn(List.of(game));

        List<BoardGame> result = service.searchByTitle("   ");

        assertEquals(1, result.size());
        verify(boardGameRepository).findAll();
    }

    @Test
    void searchByTitleUsesRepositoryQuery() {
        BoardGame game = new BoardGame();
        when(boardGameRepository.findByTitleContainingIgnoreCase("wonders")).thenReturn(List.of(game));

        List<BoardGame> result = service.searchByTitle(" wonders ");

        assertEquals(1, result.size());
        verify(boardGameRepository).findByTitleContainingIgnoreCase("wonders");
    }

    @Test
    void createResolvesRelationsAndPersists() {
        UUID pubId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        Publisher publisher = new Publisher();
        publisher.setId(pubId);

        Author author = new Author();
        author.setId(authorId);

        Category category = new Category();
        category.setId(categoryId);

        BoardGame source = new BoardGame();
        source.setTitle("7 Wonders");
        source.setDescription("jeu de cartes");
        source.setPrice(new BigDecimal("39.99"));
        source.setAverageRating(new BigDecimal("4.50"));
        source.setReleaseDate(LocalDate.of(2024, 1, 1));
        source.setPublisher(publisher);
        source.setAuthors(Set.of(author));
        source.setCategories(Set.of(category));

        when(publisherRepository.findById(pubId)).thenReturn(Optional.of(publisher));
        when(authorRepository.findAllById(Set.of(authorId))).thenReturn(List.of(author));
        when(categoryRepository.findAllById(Set.of(categoryId))).thenReturn(List.of(category));
        when(boardGameRepository.save(any(BoardGame.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BoardGame saved = service.create(source);

        assertEquals("7 Wonders", saved.getTitle());
        assertEquals(pubId, saved.getPublisher().getId());
        assertEquals(1, saved.getAuthors().size());
        assertEquals(1, saved.getCategories().size());
    }

    @Test
    void createFailsWhenPublisherMissing() {
        BoardGame source = new BoardGame();
        source.setPublisher(new Publisher());

        assertThrows(ResourceNotFoundException.class, () -> service.create(source));
    }

    @Test
    void createFailsWhenAnyAuthorIsMissing() {
        UUID pubId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        Publisher publisher = new Publisher();
        publisher.setId(pubId);

        Author author = new Author();
        author.setId(authorId);

        BoardGame source = new BoardGame();
        source.setPublisher(publisher);
        source.setAuthors(Set.of(author));

        when(publisherRepository.findById(pubId)).thenReturn(Optional.of(publisher));
        when(authorRepository.findAllById(Set.of(authorId))).thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class, () -> service.create(source));
    }

    @Test
    void updateFailsWhenCategoryIsMissing() {
        UUID gameId = UUID.randomUUID();
        UUID pubId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        BoardGame existing = new BoardGame();
        existing.setPublisher(new Publisher());

        Publisher publisher = new Publisher();
        publisher.setId(pubId);

        Category category = new Category();
        category.setId(categoryId);

        BoardGame update = new BoardGame();
        update.setPublisher(publisher);
        update.setCategories(Set.of(category));

        when(boardGameRepository.findById(gameId)).thenReturn(Optional.of(existing));
        when(publisherRepository.findById(pubId)).thenReturn(Optional.of(publisher));
        when(categoryRepository.findAllById(Set.of(categoryId))).thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class, () -> service.update(gameId, update));
    }
}
