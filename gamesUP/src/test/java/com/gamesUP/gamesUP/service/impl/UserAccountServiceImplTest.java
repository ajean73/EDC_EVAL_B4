package com.gamesUP.gamesUP.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
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
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceImplTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserAccountServiceImpl service;

    @Test
    void createEncodesPasswordAndDefaultsRole() {
        UserAccount input = new UserAccount();
        input.setPasswordHash("plain");

        when(passwordEncoder.encode("plain")).thenReturn("encoded");
        when(userAccountRepository.save(input)).thenReturn(input);

        UserAccount created = service.create(input);

        assertEquals("encoded", created.getPasswordHash());
        assertEquals(UserRole.CUSTOMER, created.getRole());
    }

    @Test
    void createKeepsAlreadyEncodedPassword() {
        UserAccount input = new UserAccount();
        input.setPasswordHash("$2b$alreadyEncoded");
        input.setRole(UserRole.ADMIN);

        when(userAccountRepository.save(input)).thenReturn(input);

        UserAccount created = service.create(input);

        assertEquals("$2b$alreadyEncoded", created.getPasswordHash());
        assertEquals(UserRole.ADMIN, created.getRole());
    }

    @Test
    void createRejectsBlankPassword() {
        UserAccount input = new UserAccount();
        input.setPasswordHash(" ");

        assertThrows(IllegalArgumentException.class, () -> service.create(input));
    }

    @Test
    void updateEncodesPasswordAndPreservesRoleWhenMissing() {
        UUID id = UUID.randomUUID();

        UserAccount existing = new UserAccount();
        existing.setRole(UserRole.ADMIN);

        UserAccount update = new UserAccount();
        update.setFirstName("Claire");
        update.setLastName("Dubois");
        update.setEmail("claire.dubois@exemple.fr");
        update.setPasswordHash("raw");

        when(userAccountRepository.findById(id)).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-new");
        when(userAccountRepository.save(existing)).thenReturn(existing);

        UserAccount saved = service.update(id, update);

        assertEquals("Claire", saved.getFirstName());
        assertEquals("encoded-new", saved.getPasswordHash());
        assertEquals(UserRole.ADMIN, saved.getRole());
    }

    @Test
    void deleteLoadsEntityThenDeletes() {
        UUID id = UUID.randomUUID();
        UserAccount existing = new UserAccount();
        when(userAccountRepository.findById(id)).thenReturn(Optional.of(existing));

        service.delete(id);

        verify(userAccountRepository).delete(existing);
    }

    @Test
    void updateWithEncodedPasswordDoesNotReencode() {
        UUID id = UUID.randomUUID();

        UserAccount existing = new UserAccount();
        existing.setPasswordHash("old");
        existing.setRole(UserRole.CUSTOMER);

        UserAccount update = new UserAccount();
        update.setPasswordHash("$2a$alreadyEncoded");

        when(userAccountRepository.findById(id)).thenReturn(Optional.of(existing));
        when(userAccountRepository.save(existing)).thenReturn(existing);

        UserAccount saved = service.update(id, update);

        assertNotEquals("old", saved.getPasswordHash());
        assertEquals("$2a$alreadyEncoded", saved.getPasswordHash());
    }
}
