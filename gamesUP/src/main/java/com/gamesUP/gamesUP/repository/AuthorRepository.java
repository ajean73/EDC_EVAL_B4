package com.gamesUP.gamesUP.repository;

import com.gamesUP.gamesUP.domain.Author;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<Author, UUID> {
}
