package com.gamesUP.gamesUP.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gamesUP.gamesUP.domain.Publisher;
import com.gamesUP.gamesUP.repository.PublisherRepository;
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
class PublisherServiceImplTest {

    @Mock
    private PublisherRepository publisherRepository;

    private PublisherServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PublisherServiceImpl(publisherRepository);
    }

    @Test
    void findAllReturnsRepositoryValues() {
        Publisher p1 = publisher("P1", "FR");
        Publisher p2 = publisher("P2", "US");
        when(publisherRepository.findAll()).thenReturn(List.of(p1, p2));

        List<Publisher> result = service.findAll();

        assertEquals(2, result.size());
        assertSame(p1, result.get(0));
        verify(publisherRepository).findAll();
    }

    @Test
    void findByIdReturnsEntityWhenPresent() {
        UUID id = UUID.randomUUID();
        Publisher existing = publisher("Ludonaute", "FR");
        existing.setId(id);
        when(publisherRepository.findById(id)).thenReturn(Optional.of(existing));

        Publisher result = service.findById(id);

        assertSame(existing, result);
    }

    @Test
    void findByIdThrowsWhenMissing() {
        UUID id = UUID.randomUUID();
        when(publisherRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(id));
    }

    @Test
    void createSavesAndReturnsEntity() {
        Publisher toCreate = publisher("Space Cowboys", "FR");
        when(publisherRepository.save(toCreate)).thenReturn(toCreate);

        Publisher result = service.create(toCreate);

        assertSame(toCreate, result);
        verify(publisherRepository).save(toCreate);
    }

    @Test
    void updateCopiesFieldsAndSaves() {
        UUID id = UUID.randomUUID();
        Publisher existing = publisher("Old", "UK");
        existing.setId(id);
        Publisher incoming = publisher("New", "JP");

        when(publisherRepository.findById(id)).thenReturn(Optional.of(existing));
        when(publisherRepository.save(existing)).thenReturn(existing);

        Publisher result = service.update(id, incoming);

        assertEquals("New", result.getName());
        assertEquals("JP", result.getCountry());
        verify(publisherRepository).save(existing);
    }

    @Test
    void deleteRemovesExistingEntity() {
        UUID id = UUID.randomUUID();
        Publisher existing = publisher("ToDelete", "DE");
        existing.setId(id);
        when(publisherRepository.findById(id)).thenReturn(Optional.of(existing));

        service.delete(id);

        verify(publisherRepository).delete(existing);
    }

    private Publisher publisher(String name, String country) {
        Publisher publisher = new Publisher();
        publisher.setName(name);
        publisher.setCountry(country);
        return publisher;
    }
}