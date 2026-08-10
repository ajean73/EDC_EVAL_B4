package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.domain.Publisher;
import java.util.List;
import java.util.UUID;

public interface PublisherService {
    List<Publisher> findAll();

    Publisher findById(UUID id);

    Publisher create(Publisher publisher);

    Publisher update(UUID id, Publisher publisher);

    void delete(UUID id);
}
