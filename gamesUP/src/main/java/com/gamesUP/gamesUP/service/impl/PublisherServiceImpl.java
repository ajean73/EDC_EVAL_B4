package com.gamesUP.gamesUP.service.impl;

import com.gamesUP.gamesUP.domain.Publisher;
import com.gamesUP.gamesUP.repository.PublisherRepository;
import com.gamesUP.gamesUP.service.PublisherService;
import com.gamesUP.gamesUP.service.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PublisherServiceImpl implements PublisherService {

    private final PublisherRepository publisherRepository;

    public PublisherServiceImpl(PublisherRepository publisherRepository) {
        this.publisherRepository = publisherRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Publisher> findAll() {
        return publisherRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Publisher findById(UUID id) {
        return publisherRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Publisher not found: " + id));
    }

    @Override
    public Publisher create(Publisher publisher) {
        return publisherRepository.save(publisher);
    }

    @Override
    public Publisher update(UUID id, Publisher publisher) {
        Publisher existing = findById(id);
        existing.setName(publisher.getName());
        existing.setCountry(publisher.getCountry());
        return publisherRepository.save(existing);
    }

    @Override
    public void delete(UUID id) {
        Publisher existing = findById(id);
        publisherRepository.delete(existing);
    }
}
