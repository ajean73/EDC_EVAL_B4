package com.gamesUP.gamesUP.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamesUP.gamesUP.domain.Author;
import com.gamesUP.gamesUP.domain.BoardGame;
import com.gamesUP.gamesUP.domain.Category;
import com.gamesUP.gamesUP.domain.Publisher;
import com.gamesUP.gamesUP.domain.UserRole;
import com.gamesUP.gamesUP.service.AuthService;
import com.gamesUP.gamesUP.service.BoardGameService;
import com.gamesUP.gamesUP.web.dto.AuthLoginRequest;
import com.gamesUP.gamesUP.web.dto.AuthResponse;
import com.gamesUP.gamesUP.web.dto.BoardGameRequest;
import com.gamesUP.gamesUP.web.dto.RefreshTokenRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BoardGameService boardGameService;

    @MockBean
    private AuthService authService;

    @Test
    void publicCatalogGetIsAccessibleWithoutAuthentication() throws Exception {
        UUID pubId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        BoardGame game = new BoardGame();
        game.setId(UUID.randomUUID());
        game.setTitle("7 Wonders");
        game.setDescription("jeu de cartes");
        game.setPrice(new BigDecimal("39.90"));
        game.setAverageRating(new BigDecimal("4.10"));
        game.setReleaseDate(LocalDate.of(2024, 1, 1));

        Publisher publisher = new Publisher();
        publisher.setId(pubId);
        game.setPublisher(publisher);

        Author author = new Author();
        author.setId(authorId);
        game.setAuthors(Set.of(author));

        Category category = new Category();
        category.setId(categoryId);
        game.setCategories(Set.of(category));

        when(boardGameService.searchByTitle(null)).thenReturn(List.of(game));

        mockMvc.perform(get("/api/v1/catalog/games"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].title").value("7 Wonders"));
    }

    @Test
    void catalogPostIsForbiddenForAnonymous() throws Exception {
        BoardGameRequest request = new BoardGameRequest(
            "7 Wonders",
            "jeu de cartes",
            new BigDecimal("39.90"),
            new BigDecimal("4.10"),
            LocalDate.of(2024, 1, 1),
            UUID.randomUUID(),
            Set.of(UUID.randomUUID()),
            Set.of(UUID.randomUUID())
        );

        mockMvc.perform(post("/api/v1/catalog/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }

    @Test
    void catalogPostIsAllowedForAdmin() throws Exception {
        UUID pubId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        BoardGameRequest request = new BoardGameRequest(
            "7 Wonders",
            "jeu de cartes",
            new BigDecimal("39.90"),
            new BigDecimal("4.10"),
            LocalDate.of(2024, 1, 1),
            pubId,
            Set.of(authorId),
            Set.of(categoryId)
        );

        BoardGame saved = new BoardGame();
        saved.setId(UUID.randomUUID());
        saved.setTitle("7 Wonders");
        saved.setDescription("jeu de cartes");
        saved.setPrice(new BigDecimal("39.90"));
        saved.setAverageRating(new BigDecimal("4.10"));
        saved.setReleaseDate(LocalDate.of(2024, 1, 1));

        Publisher publisher = new Publisher();
        publisher.setId(pubId);
        saved.setPublisher(publisher);

        Author author = new Author();
        author.setId(authorId);
        saved.setAuthors(Set.of(author));

        Category category = new Category();
        category.setId(categoryId);
        saved.setCategories(Set.of(category));

        when(boardGameService.create(any(BoardGame.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/catalog/games")
                .with(user("admin").roles("ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("7 Wonders"));
    }

    @Test
    void authEndpointsAreAccessibleWithoutAuthentication() throws Exception {
        UUID userId = UUID.randomUUID();
        when(authService.login(any(AuthLoginRequest.class))).thenReturn(
            new AuthResponse("a", "r", "Bearer", 900000L, userId, "joueur@exemple.fr", UserRole.CUSTOMER)
        );
        when(authService.refresh(any(RefreshTokenRequest.class))).thenReturn(
            new AuthResponse("a2", "r2", "Bearer", 900000L, userId, "joueur@exemple.fr", UserRole.CUSTOMER)
        );

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"joueur@exemple.fr\",\"password\":\"pass\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tokenType").value("Bearer"));

        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"token\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.refreshToken").value("r2"));
    }
}
