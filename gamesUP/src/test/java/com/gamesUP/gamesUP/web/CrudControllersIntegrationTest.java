package com.gamesUP.gamesUP.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamesUP.gamesUP.domain.Author;
import com.gamesUP.gamesUP.domain.BoardGame;
import com.gamesUP.gamesUP.domain.Category;
import com.gamesUP.gamesUP.domain.OrderLine;
import com.gamesUP.gamesUP.domain.OrderStatus;
import com.gamesUP.gamesUP.domain.Publisher;
import com.gamesUP.gamesUP.domain.PurchaseOrder;
import com.gamesUP.gamesUP.domain.UserAccount;
import com.gamesUP.gamesUP.domain.UserRole;
import com.gamesUP.gamesUP.service.AuthorService;
import com.gamesUP.gamesUP.service.CategoryService;
import com.gamesUP.gamesUP.service.PublisherService;
import com.gamesUP.gamesUP.service.PurchaseOrderService;
import com.gamesUP.gamesUP.service.UserAccountService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
class CrudControllersIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthorService authorService;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private PublisherService publisherService;

    @MockBean
    private UserAccountService userAccountService;

    @MockBean
    private PurchaseOrderService purchaseOrderService;

    @Test
    void authorCategoryPublisherCrudEndpointsAreMapped() throws Exception {
        UUID id = UUID.randomUUID();

        Author author = new Author();
        author.setId(id);
        author.setName("Bruno Cathala");

        when(authorService.findAll()).thenReturn(List.of(author));
        when(authorService.findById(id)).thenReturn(author);
        when(authorService.create(any(Author.class))).thenReturn(author);
        when(authorService.update(any(UUID.class), any(Author.class))).thenReturn(author);

        mockMvc.perform(get("/api/v1/catalog/authors"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Bruno Cathala"));

        mockMvc.perform(get("/api/v1/catalog/authors/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Bruno Cathala"));

        mockMvc.perform(post("/api/v1/catalog/authors")
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Bruno Cathala\"}"))
            .andExpect(status().isCreated());

        mockMvc.perform(put("/api/v1/catalog/authors/{id}", id)
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Bruno Cathala\"}"))
            .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/catalog/authors/{id}", id)
                .with(user("admin").roles("ADMIN")))
            .andExpect(status().isNoContent());

        Category category = new Category();
        category.setId(id);
        category.setName("Strategie");
        category.setDescription("jeux experts");

        when(categoryService.findAll()).thenReturn(List.of(category));
        when(categoryService.findById(id)).thenReturn(category);
        when(categoryService.create(any(Category.class))).thenReturn(category);
        when(categoryService.update(any(UUID.class), any(Category.class))).thenReturn(category);

        mockMvc.perform(get("/api/v1/catalog/categories"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Strategie"));

        mockMvc.perform(get("/api/v1/catalog/categories/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.description").value("jeux experts"));

        mockMvc.perform(post("/api/v1/catalog/categories")
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Strategie\",\"description\":\"jeux experts\"}"))
            .andExpect(status().isCreated());

        mockMvc.perform(put("/api/v1/catalog/categories/{id}", id)
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Strategie\",\"description\":\"jeux experts\"}"))
            .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/catalog/categories/{id}", id)
                .with(user("admin").roles("ADMIN")))
            .andExpect(status().isNoContent());

        Publisher publisher = new Publisher();
        publisher.setId(id);
        publisher.setName("Ludonaute");
        publisher.setCountry("FR");

        when(publisherService.findAll()).thenReturn(List.of(publisher));
        when(publisherService.findById(id)).thenReturn(publisher);
        when(publisherService.create(any(Publisher.class))).thenReturn(publisher);
        when(publisherService.update(any(UUID.class), any(Publisher.class))).thenReturn(publisher);

        mockMvc.perform(get("/api/v1/catalog/publishers"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Ludonaute"));

        mockMvc.perform(get("/api/v1/catalog/publishers/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.country").value("FR"));

        mockMvc.perform(post("/api/v1/catalog/publishers")
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Ludonaute\",\"country\":\"FR\"}"))
            .andExpect(status().isCreated());

        mockMvc.perform(put("/api/v1/catalog/publishers/{id}", id)
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Ludonaute\",\"country\":\"FR\"}"))
            .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/catalog/publishers/{id}", id)
                .with(user("admin").roles("ADMIN")))
            .andExpect(status().isNoContent());
    }

    @Test
    void identityAndCommerceControllersAreCovered() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();

        UserAccount user = new UserAccount();
        user.setId(userId);
        user.setFirstName("Claire");
        user.setLastName("Dubois");
        user.setEmail("claire.dubois@exemple.fr");
        user.setPasswordHash("hash");
        user.setRole(UserRole.CUSTOMER);

        when(userAccountService.findAll()).thenReturn(List.of(user));
        when(userAccountService.findById(userId)).thenReturn(user);
        when(userAccountService.create(any(UserAccount.class))).thenReturn(user);
        when(userAccountService.update(any(UUID.class), any(UserAccount.class))).thenReturn(user);

        mockMvc.perform(get("/api/v1/identity/users").with(user("admin").roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].email").value("claire.dubois@exemple.fr"));

        mockMvc.perform(get("/api/v1/identity/users/{id}", userId).with(user("admin").roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("Claire"));

        mockMvc.perform(post("/api/v1/identity/users")
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"Claire\",\"lastName\":\"Dubois\",\"email\":\"claire.dubois@exemple.fr\",\"passwordHash\":\"hash\",\"role\":\"CUSTOMER\"}"))
            .andExpect(status().isCreated());

        mockMvc.perform(put("/api/v1/identity/users/{id}", userId)
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"Claire\",\"lastName\":\"Dubois\",\"email\":\"claire.dubois@exemple.fr\",\"passwordHash\":\"hash\",\"role\":\"ADMIN\"}"))
            .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/identity/users/{id}", userId).with(user("admin").roles("ADMIN")))
            .andExpect(status().isNoContent());

        BoardGame game = new BoardGame();
        game.setId(gameId);

        OrderLine line = new OrderLine();
        line.setGame(game);
        line.setQuantity(2);
        line.setUnitPrice(new BigDecimal("12.50"));

        PurchaseOrder order = new PurchaseOrder();
        order.setUser(user);
        order.setOrderedAt(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("25.00"));
        order.setShippingAddress("12 rue de la Paix");
        order.setLines(List.of(line));

        when(purchaseOrderService.findAll()).thenReturn(List.of(order));
        when(purchaseOrderService.findById(orderId)).thenReturn(order);
        when(purchaseOrderService.create(any(PurchaseOrder.class))).thenReturn(order);
        when(purchaseOrderService.update(any(UUID.class), any(PurchaseOrder.class))).thenReturn(order);

        String requestBody = objectMapper.writeValueAsString(new com.gamesUP.gamesUP.web.dto.PurchaseOrderRequest(
            userId,
            LocalDateTime.now(),
            OrderStatus.PENDING,
            "12 rue de la Paix",
            List.of(new com.gamesUP.gamesUP.web.dto.OrderLineRequest(gameId, 2, new BigDecimal("12.50")))
        ));

        mockMvc.perform(get("/api/v1/commerce/orders").with(user("admin").roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].shippingAddress").value("12 rue de la Paix"));

        mockMvc.perform(get("/api/v1/commerce/orders/{id}", orderId).with(user("admin").roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalAmount").value(25.00));

        mockMvc.perform(post("/api/v1/commerce/orders")
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated());

        mockMvc.perform(put("/api/v1/commerce/orders/{id}", orderId)
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/commerce/orders/{id}", orderId).with(user("admin").roles("ADMIN")))
            .andExpect(status().isNoContent());
    }
}
