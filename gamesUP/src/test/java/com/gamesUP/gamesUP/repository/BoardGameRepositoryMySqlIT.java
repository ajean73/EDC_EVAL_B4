package com.gamesUP.gamesUP.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gamesUP.gamesUP.domain.Author;
import com.gamesUP.gamesUP.domain.BoardGame;
import com.gamesUP.gamesUP.domain.Category;
import com.gamesUP.gamesUP.domain.Publisher;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
@EnabledIfEnvironmentVariable(named = "RUN_MYSQL_IT", matches = "true")
class BoardGameRepositoryMySqlIT {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4")
        .withDatabaseName("gamesup_test")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private BoardGameRepository boardGameRepository;

    @Autowired
    private PublisherRepository publisherRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void findByTitleContainingIgnoreCaseWorksOnMySqlContainer() {
        Publisher publisher = new Publisher();
        publisher.setName("Ludonaute");
        publisher.setCountry("FR");
        publisher = publisherRepository.save(publisher);

        Author author = new Author();
        author.setName("Bruno Cathala");
        author = authorRepository.save(author);

        Category category = new Category();
        category.setName("Strategie");
        category.setDescription("jeux experts");
        category = categoryRepository.save(category);

        BoardGame game = new BoardGame();
        game.setTitle("7 Wonders Duel");
        game.setDescription("jeu de cartes");
        game.setPrice(new BigDecimal("24.99"));
        game.setAverageRating(new BigDecimal("4.50"));
        game.setReleaseDate(LocalDate.of(2015, 1, 1));
        game.setPublisher(publisher);
        game.setAuthors(Set.of(author));
        game.setCategories(Set.of(category));
        boardGameRepository.save(game);

        assertEquals(1, boardGameRepository.findByTitleContainingIgnoreCase("WONDERS").size());
    }
}