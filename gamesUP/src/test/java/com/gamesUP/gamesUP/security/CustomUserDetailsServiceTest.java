package com.gamesUP.gamesUP.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.gamesUP.gamesUP.domain.UserAccount;
import com.gamesUP.gamesUP.domain.UserRole;
import com.gamesUP.gamesUP.repository.UserAccountRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @InjectMocks
    private CustomUserDetailsService service;

    @Test
    void loadUserByUsernameBuildsSpringUser() {
        UserAccount user = new UserAccount();
        user.setId(UUID.randomUUID());
        user.setEmail("admin@exemple.fr");
        user.setPasswordHash("$2b$hash");
        user.setRole(UserRole.ADMIN);

        when(userAccountRepository.findByEmail("admin@exemple.fr")).thenReturn(Optional.of(user));

        var details = service.loadUserByUsername("admin@exemple.fr");

        assertEquals("admin@exemple.fr", details.getUsername());
        assertEquals("$2b$hash", details.getPassword());
        assertEquals("ROLE_ADMIN", details.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void loadUserByUsernameThrowsWhenMissing() {
        when(userAccountRepository.findByEmail("inconnu@exemple.fr")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("inconnu@exemple.fr"));
    }
}
