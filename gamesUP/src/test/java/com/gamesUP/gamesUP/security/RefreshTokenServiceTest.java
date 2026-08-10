package com.gamesUP.gamesUP.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gamesUP.gamesUP.domain.RefreshToken;
import com.gamesUP.gamesUP.domain.UserAccount;
import com.gamesUP.gamesUP.repository.RefreshTokenRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private RefreshTokenService service;

    @BeforeEach
    void setUp() {
        service = new RefreshTokenService(refreshTokenRepository, 60_000L);
    }

    @Test
    void createTokenPersistsHashedToken() {
        UserAccount user = new UserAccount();
        user.setId(UUID.randomUUID());

        String token = service.createToken(user);

        assertNotNull(token);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        RefreshToken saved = captor.getValue();
        assertEquals(user, saved.getUser());
        assertNotNull(saved.getTokenHash());
        assertNotNull(saved.getExpiresAt());
    }

    @Test
    void rotateTokenRejectsUnknownToken() {
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> service.rotateToken("unknown"));
    }

    @Test
    void rotateTokenRevokesCurrentAndCreatesNext() {
        UserAccount user = new UserAccount();
        user.setId(UUID.randomUUID());

        RefreshToken current = new RefreshToken();
        current.setUser(user);
        current.setTokenHash("hash");
        current.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(current));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefreshTokenService.RotatedRefreshToken rotated = service.rotateToken("raw-token");

        assertEquals(user, rotated.user());
        assertNotNull(rotated.refreshToken());
        assertNotNull(current.getRevokedAt());
        assertNotNull(current.getReplacedByHash());
        verify(refreshTokenRepository).save(current);
    }

    @Test
    void revokeTokenMarksExistingTokenAsRevoked() {
        RefreshToken token = new RefreshToken();
        token.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(token));

        service.revokeToken("raw-token");

        assertNotNull(token.getRevokedAt());
        verify(refreshTokenRepository).save(token);
    }

    @Test
    void cleanupExpiredDelegatesToRepository() {
        when(refreshTokenRepository.deleteExpired(any())).thenReturn(3);

        assertEquals(3, service.cleanupExpired());
        assertEquals(60_000L, service.getRefreshExpirationMs());
    }
}
