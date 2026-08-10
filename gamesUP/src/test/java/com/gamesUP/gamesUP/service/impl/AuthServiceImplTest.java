package com.gamesUP.gamesUP.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gamesUP.gamesUP.domain.UserAccount;
import com.gamesUP.gamesUP.domain.UserRole;
import com.gamesUP.gamesUP.repository.UserAccountRepository;
import com.gamesUP.gamesUP.security.JwtTokenService;
import com.gamesUP.gamesUP.security.RefreshTokenService;
import com.gamesUP.gamesUP.web.dto.AuthLoginRequest;
import com.gamesUP.gamesUP.web.dto.AuthRegisterRequest;
import com.gamesUP.gamesUP.web.dto.AuthResponse;
import com.gamesUP.gamesUP.web.dto.RefreshTokenRequest;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthServiceImpl service;

    @Test
    void registerCreatesCustomerAndReturnsTokens() {
        AuthRegisterRequest request = new AuthRegisterRequest("Claire", "Dubois", "claire.dubois@exemple.fr", "pass");

        UserAccount saved = new UserAccount();
        saved.setId(UUID.randomUUID());
        saved.setEmail("claire.dubois@exemple.fr");
        saved.setRole(UserRole.CUSTOMER);

        when(userAccountRepository.findByEmail("claire.dubois@exemple.fr")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(userAccountRepository.save(any(UserAccount.class))).thenReturn(saved);
        when(jwtTokenService.generateToken(saved)).thenReturn("access");
        when(jwtTokenService.getAccessTokenExpirationMs()).thenReturn(900_000L);
        when(refreshTokenService.createToken(saved)).thenReturn("refresh");

        AuthResponse response = service.register(request);

        assertEquals("access", response.accessToken());
        assertEquals("refresh", response.refreshToken());
        assertEquals(UserRole.CUSTOMER, response.role());

        ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userAccountRepository).save(captor.capture());
        assertEquals("encoded", captor.getValue().getPasswordHash());
        assertEquals(UserRole.CUSTOMER, captor.getValue().getRole());
    }

    @Test
    void registerRejectsDuplicateEmail() {
        AuthRegisterRequest request = new AuthRegisterRequest("Claire", "Dubois", "claire.dubois@exemple.fr", "pass");
        when(userAccountRepository.findByEmail("claire.dubois@exemple.fr")).thenReturn(Optional.of(new UserAccount()));

        assertThrows(IllegalStateException.class, () -> service.register(request));
    }

    @Test
    void loginDelegatesAuthenticationAndReturnsTokens() {
        AuthLoginRequest request = new AuthLoginRequest("claire.dubois@exemple.fr", "pass");
        UserAccount user = new UserAccount();
        user.setId(UUID.randomUUID());
        user.setEmail("claire.dubois@exemple.fr");
        user.setRole(UserRole.ADMIN);

        when(userAccountRepository.findByEmail("claire.dubois@exemple.fr")).thenReturn(Optional.of(user));
        when(jwtTokenService.generateToken(user)).thenReturn("access");
        when(jwtTokenService.getAccessTokenExpirationMs()).thenReturn(900_000L);
        when(refreshTokenService.createToken(user)).thenReturn("refresh");

        AuthResponse response = service.login(request);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        assertEquals("access", response.accessToken());
        assertEquals("refresh", response.refreshToken());
        assertEquals(UserRole.ADMIN, response.role());
    }

    @Test
    void loginPropagatesBadCredentials() {
        AuthLoginRequest request = new AuthLoginRequest("claire.dubois@exemple.fr", "wrong");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("bad"));

        assertThrows(BadCredentialsException.class, () -> service.login(request));
    }

    @Test
    void refreshRejectsBlankToken() {
        assertThrows(IllegalArgumentException.class, () -> service.refresh(new RefreshTokenRequest("  ")));
    }

    @Test
    void refreshRotatesAndReturnsNewTokens() {
        UserAccount user = new UserAccount();
        user.setId(UUID.randomUUID());
        user.setEmail("claire.dubois@exemple.fr");
        user.setRole(UserRole.CUSTOMER);

        when(refreshTokenService.rotateToken("old-refresh"))
            .thenReturn(new RefreshTokenService.RotatedRefreshToken(user, "new-refresh"));
        when(jwtTokenService.generateToken(user)).thenReturn("new-access");
        when(jwtTokenService.getAccessTokenExpirationMs()).thenReturn(900_000L);

        AuthResponse response = service.refresh(new RefreshTokenRequest("old-refresh"));

        assertEquals("new-access", response.accessToken());
        assertEquals("new-refresh", response.refreshToken());
    }

    @Test
    void logoutRejectsBlankToken() {
        assertThrows(IllegalArgumentException.class, () -> service.logout(new RefreshTokenRequest("")));
    }

    @Test
    void logoutRevokesRefreshToken() {
        service.logout(new RefreshTokenRequest("refresh-token"));
        verify(refreshTokenService).revokeToken("refresh-token");
    }
}
