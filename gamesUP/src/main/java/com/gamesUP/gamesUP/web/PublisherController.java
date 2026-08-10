package com.gamesUP.gamesUP.web;

import com.gamesUP.gamesUP.domain.Publisher;
import com.gamesUP.gamesUP.service.PublisherService;
import com.gamesUP.gamesUP.web.dto.PublisherRequest;
import com.gamesUP.gamesUP.web.dto.PublisherResponse;
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
@RequestMapping("/api/v1/catalog/publishers")
public class PublisherController {

    private final PublisherService publisherService;

    public PublisherController(PublisherService publisherService) {
        this.publisherService = publisherService;
    }

    @GetMapping
    public List<PublisherResponse> findAll() {
        return publisherService.findAll().stream()
            .map(p -> new PublisherResponse(p.getId(), p.getName(), p.getCountry()))
            .toList();
    }

    @GetMapping("/{id}")
    public PublisherResponse findById(@PathVariable UUID id) {
        Publisher publisher = publisherService.findById(id);
        return new PublisherResponse(publisher.getId(), publisher.getName(), publisher.getCountry());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PublisherResponse create(@RequestBody PublisherRequest request) {
        Publisher publisher = new Publisher();
        publisher.setName(request.name());
        publisher.setCountry(request.country());
        Publisher created = publisherService.create(publisher);
        return new PublisherResponse(created.getId(), created.getName(), created.getCountry());
    }

    @PutMapping("/{id}")
    public PublisherResponse update(@PathVariable UUID id, @RequestBody PublisherRequest request) {
        Publisher publisher = new Publisher();
        publisher.setName(request.name());
        publisher.setCountry(request.country());
        Publisher updated = publisherService.update(id, publisher);
        return new PublisherResponse(updated.getId(), updated.getName(), updated.getCountry());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        publisherService.delete(id);
    }
}
