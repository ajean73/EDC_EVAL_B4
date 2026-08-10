package com.gamesUP.gamesUP.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gamesUP.gamesUP.domain.UserAccount;
import com.gamesUP.gamesUP.domain.UserRole;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtTokenServiceTest {

    @Test
    void generateExtractAndValidateToken() {
        JwtTokenService service = new JwtTokenService("this-is-a-secret-key-with-at-least-32-chars", 900_000L);
        UserAccount user = new UserAccount();
        user.setId(UUID.randomUUID());
        user.setEmail("amelie@exemple.fr");
        user.setRole(UserRole.CUSTOMER);

        String token = service.generateToken(user);

        assertEquals("amelie@exemple.fr", service.extractUsername(token));
        assertTrue(service.isTokenValid(token, "amelie@exemple.fr"));
        assertFalse(service.isTokenValid(token, "bob@example.com"));
        assertEquals(900_000L, service.getAccessTokenExpirationMs());
    }

    @Test
    void generateTokenFailsWithShortSecret() {
        JwtTokenService service = new JwtTokenService("short-secret", 900_000L);
        UserAccount user = new UserAccount();
        user.setId(UUID.randomUUID());
        user.setEmail("amelie@exemple.fr");
        user.setRole(UserRole.CUSTOMER);

        assertThrows(IllegalStateException.class, () -> service.generateToken(user));
    }
}
