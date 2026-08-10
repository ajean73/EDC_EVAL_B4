package com.gamesUP.gamesUP.web;

import com.gamesUP.gamesUP.domain.Author;
import com.gamesUP.gamesUP.service.AuthorService;
import com.gamesUP.gamesUP.web.dto.AuthorRequest;
import com.gamesUP.gamesUP.web.dto.AuthorResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/catalog/authors")
public class AuthorController {

    private final AuthorService authorService;

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @GetMapping
    public List<AuthorResponse> findAll() {
        return authorService.findAll().stream()
            .map(author -> new AuthorResponse(author.getId(), author.getName()))
            .toList();
    }

    @GetMapping("/{id}")
    public AuthorResponse findById(@PathVariable UUID id) {
        Author author = authorService.findById(id);
        return new AuthorResponse(author.getId(), author.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AuthorResponse create(@RequestBody AuthorRequest request) {
        Author author = new Author();
        author.setName(request.name());
        Author created = authorService.create(author);
        return new AuthorResponse(created.getId(), created.getName());
    }

    @PutMapping("/{id}")
    public AuthorResponse update(@PathVariable UUID id, @RequestBody AuthorRequest request) {
        Author author = new Author();
        author.setName(request.name());
        Author updated = authorService.update(id, author);
        return new AuthorResponse(updated.getId(), updated.getName());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        authorService.delete(id);
    }
}
