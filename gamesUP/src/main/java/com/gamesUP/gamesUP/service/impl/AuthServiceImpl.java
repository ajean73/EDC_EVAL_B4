package com.gamesUP.gamesUP.service.impl;

import com.gamesUP.gamesUP.domain.UserAccount;
import com.gamesUP.gamesUP.domain.UserRole;
import com.gamesUP.gamesUP.repository.UserAccountRepository;
import com.gamesUP.gamesUP.security.JwtTokenService;
import com.gamesUP.gamesUP.security.RefreshTokenService;
import com.gamesUP.gamesUP.service.AuthService;
import com.gamesUP.gamesUP.web.dto.AuthLoginRequest;
import com.gamesUP.gamesUP.web.dto.AuthRegisterRequest;
import com.gamesUP.gamesUP.web.dto.AuthResponse;
import com.gamesUP.gamesUP.web.dto.RefreshTokenRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenService refreshTokenService;

    public AuthServiceImpl(
        UserAccountRepository userAccountRepository,
        PasswordEncoder passwordEncoder,
        AuthenticationManager authenticationManager,
        JwtTokenService jwtTokenService,
        RefreshTokenService refreshTokenService
    ) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    public AuthResponse register(AuthRegisterRequest request) {
        if (userAccountRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalStateException("Email already in use");
        }

        // Le mot de passe est systématiquement stocké sous forme de hash.
        UserAccount user = new UserAccount();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.CUSTOMER);

        UserAccount saved = userAccountRepository.save(user);
        return issueTokens(saved);
    }

    @Override
    public AuthResponse login(AuthLoginRequest request) {
        try {
            // Validation des identifiants via le provider Spring Security configuré.
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (AuthenticationException ex) {
            throw ex;
        }

        UserAccount user = userAccountRepository.findByEmail(request.email())
            .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        return issueTokens(user);
    }

    @Override
    public AuthResponse refresh(RefreshTokenRequest request) {
        if (request == null || request.refreshToken() == null || request.refreshToken().isBlank()) {
            throw new IllegalArgumentException("Refresh token is required");
        }

        RefreshTokenService.RotatedRefreshToken rotated = refreshTokenService.rotateToken(request.refreshToken());
        UserAccount user = rotated.user();
        String accessToken = jwtTokenService.generateToken(user);

        return new AuthResponse(
            accessToken,
            rotated.refreshToken(),
            "Bearer",
            jwtTokenService.getAccessTokenExpirationMs(),
            user.getId(),
            user.getEmail(),
            user.getRole()
        );
    }

    @Override
    public void logout(RefreshTokenRequest request) {
        if (request == null || request.refreshToken() == null || request.refreshToken().isBlank()) {
            throw new IllegalArgumentException("Refresh token is required");
        }

        refreshTokenService.revokeToken(request.refreshToken());
    }

    private AuthResponse issueTokens(UserAccount user) {
        String accessToken = jwtTokenService.generateToken(user);
        String refreshToken = refreshTokenService.createToken(user);

        return new AuthResponse(
            accessToken,
            refreshToken,
            "Bearer",
            jwtTokenService.getAccessTokenExpirationMs(),
            user.getId(),
            user.getEmail(),
            user.getRole()
        );
    }
}
