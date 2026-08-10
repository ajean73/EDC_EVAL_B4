package com.gamesUP.gamesUP.repository;

import com.gamesUP.gamesUP.domain.Publisher;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublisherRepository extends JpaRepository<Publisher, UUID> {
}
