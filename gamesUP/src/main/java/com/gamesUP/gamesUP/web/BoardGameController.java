package com.gamesUP.gamesUP.web;

import com.gamesUP.gamesUP.domain.Author;
import com.gamesUP.gamesUP.domain.BoardGame;
import com.gamesUP.gamesUP.domain.Category;
import com.gamesUP.gamesUP.domain.Publisher;
import com.gamesUP.gamesUP.service.BoardGameService;
import com.gamesUP.gamesUP.web.dto.BoardGameRequest;
import com.gamesUP.gamesUP.web.dto.BoardGameResponse;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/catalog/games")
public class BoardGameController {

    private final BoardGameService boardGameService;

    public BoardGameController(BoardGameService boardGameService) {
        this.boardGameService = boardGameService;
    }

    @GetMapping
    public List<BoardGameResponse> findAll(
        @RequestParam(name = "title", required = false) String title,
        @RequestParam(name = "q", required = false) String q
    ) {
        String searchTerm = title != null ? title : q;
        return boardGameService.searchByTitle(searchTerm).stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public BoardGameResponse findById(@PathVariable UUID id) {
        return toResponse(boardGameService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BoardGameResponse create(@RequestBody BoardGameRequest request) {
        BoardGame created = boardGameService.create(toEntity(request));
        return toResponse(created);
    }

    @PutMapping("/{id}")
    public BoardGameResponse update(@PathVariable UUID id, @RequestBody BoardGameRequest request) {
        BoardGame updated = boardGameService.update(id, toEntity(request));
        return toResponse(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        boardGameService.delete(id);
    }

    private BoardGame toEntity(BoardGameRequest request) {
        BoardGame game = new BoardGame();
        game.setTitle(request.title());
        game.setDescription(request.description());
        game.setPrice(request.price());
        game.setAverageRating(request.averageRating());
        game.setReleaseDate(request.releaseDate());

        Publisher publisher = new Publisher();
        publisher.setId(request.publisherId());
        game.setPublisher(publisher);

        Set<Author> authors = new HashSet<>();
        if (request.authorIds() != null) {
            request.authorIds().forEach(id -> {
                Author author = new Author();
                author.setId(id);
                authors.add(author);
            });
        }
        game.setAuthors(authors);

        Set<Category> categories = new HashSet<>();
        if (request.categoryIds() != null) {
            request.categoryIds().forEach(id -> {
                Category category = new Category();
                category.setId(id);
                categories.add(category);
            });
        }
        game.setCategories(categories);

        return game;
    }

    private BoardGameResponse toResponse(BoardGame game) {
        Set<UUID> authorIds = game.getAuthors().stream().map(Author::getId).collect(java.util.stream.Collectors.toSet());
        Set<UUID> categoryIds = game.getCategories().stream().map(Category::getId).collect(java.util.stream.Collectors.toSet());

        return new BoardGameResponse(
            game.getId(),
            game.getTitle(),
            game.getDescription(),
            game.getPrice(),
            game.getAverageRating(),
            game.getReleaseDate(),
            game.getPublisher().getId(),
            authorIds,
            categoryIds
        );
    }
}
