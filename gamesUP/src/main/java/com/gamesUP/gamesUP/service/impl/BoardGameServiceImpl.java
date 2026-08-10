package com.gamesUP.gamesUP.service.impl;

import com.gamesUP.gamesUP.domain.Author;
import com.gamesUP.gamesUP.domain.BoardGame;
import com.gamesUP.gamesUP.domain.Category;
import com.gamesUP.gamesUP.domain.Publisher;
import com.gamesUP.gamesUP.repository.AuthorRepository;
import com.gamesUP.gamesUP.repository.BoardGameRepository;
import com.gamesUP.gamesUP.repository.CategoryRepository;
import com.gamesUP.gamesUP.repository.PublisherRepository;
import com.gamesUP.gamesUP.service.BoardGameService;
import com.gamesUP.gamesUP.service.ResourceNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BoardGameServiceImpl implements BoardGameService {

    private final BoardGameRepository boardGameRepository;
    private final PublisherRepository publisherRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;

    public BoardGameServiceImpl(
        BoardGameRepository boardGameRepository,
        PublisherRepository publisherRepository,
        AuthorRepository authorRepository,
        CategoryRepository categoryRepository
    ) {
        this.boardGameRepository = boardGameRepository;
        this.publisherRepository = publisherRepository;
        this.authorRepository = authorRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoardGame> findAll() {
        return boardGameRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public BoardGame findById(UUID id) {
        return boardGameRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Game not found: " + id));
    }

    @Override
    public BoardGame create(BoardGame boardGame) {
        BoardGame entity = new BoardGame();
        applyUpdatableFields(entity, boardGame);
        return boardGameRepository.save(entity);
    }

    @Override
    public BoardGame update(UUID id, BoardGame boardGame) {
        BoardGame existing = findById(id);
        applyUpdatableFields(existing, boardGame);
        return boardGameRepository.save(existing);
    }

    @Override
    public void delete(UUID id) {
        BoardGame existing = findById(id);
        boardGameRepository.delete(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoardGame> searchByTitle(String title) {
        if (title == null || title.isBlank()) {
            return findAll();
        }
        return boardGameRepository.findByTitleContainingIgnoreCase(title.trim());
    }

    private void applyUpdatableFields(BoardGame target, BoardGame source) {
        target.setTitle(source.getTitle());
        target.setDescription(source.getDescription());
        target.setPrice(source.getPrice());
        target.setAverageRating(source.getAverageRating());
        target.setReleaseDate(source.getReleaseDate());

        Publisher publisher = resolvePublisher(source.getPublisher());
        target.setPublisher(publisher);

        target.setAuthors(resolveAuthors(source.getAuthors()));
        target.setCategories(resolveCategories(source.getCategories()));
    }

    private Publisher resolvePublisher(Publisher publisher) {
        if (publisher == null || publisher.getId() == null) {
            throw new ResourceNotFoundException("Publisher id is required");
        }
        return publisherRepository.findById(publisher.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Publisher not found: " + publisher.getId()));
    }

    private Set<Author> resolveAuthors(Set<Author> authors) {
        if (authors == null || authors.isEmpty()) {
            return new HashSet<>();
        }

        Set<UUID> ids = authors.stream()
            .map(Author::getId)
            .collect(Collectors.toSet());

        List<Author> managedAuthors = authorRepository.findAllById(ids);
        if (managedAuthors.size() != ids.size()) {
            throw new ResourceNotFoundException("One or more authors were not found");
        }
        return new HashSet<>(managedAuthors);
    }

    private Set<Category> resolveCategories(Set<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return new HashSet<>();
        }

        Set<UUID> ids = categories.stream()
            .map(Category::getId)
            .collect(Collectors.toSet());

        List<Category> managedCategories = categoryRepository.findAllById(ids);
        if (managedCategories.size() != ids.size()) {
            throw new ResourceNotFoundException("One or more categories were not found");
        }
        return new HashSet<>(managedCategories);
    }
}
