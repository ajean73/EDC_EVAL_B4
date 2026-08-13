package com.gamesUP.gamesUP.security;

import com.gamesUP.gamesUP.domain.RefreshToken;
import com.gamesUP.gamesUP.domain.UserAccount;
import com.gamesUP.gamesUP.repository.RefreshTokenRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshExpirationMs;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(
        RefreshTokenRepository refreshTokenRepository,
        @Value("${jwt.refresh-expiration-ms:604800000}") long refreshExpirationMs
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public String createToken(UserAccount user) {
        Objects.requireNonNull(user, "user must not be null");

        String rawToken = generateRawToken();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        // Seul le hash est persisté pour limiter l'impact d'une fuite de base.
        refreshToken.setTokenHash(hashToken(rawToken));
        refreshToken.setExpiresAt(LocalDateTime.now().plusNanos(toNanos(refreshExpirationMs)));

        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    public RotatedRefreshToken rotateToken(String rawToken) {
        String tokenHash = hashToken(rawToken);

        RefreshToken current = refreshTokenRepository.findByTokenHash(tokenHash)
            .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        if (!current.isActive()) {
            throw new BadCredentialsException("Refresh token expired or revoked");
        }

        // Rotation stricte: ancien token révoqué puis nouveau token émis.
        String nextRawToken = generateRawToken();
        String nextHash = hashToken(nextRawToken);

        LocalDateTime now = LocalDateTime.now();
        current.setRevokedAt(LocalDateTime.now());
        current.setReplacedByHash(nextHash);
        refreshTokenRepository.save(current);

        RefreshToken next = new RefreshToken();
        next.setUser(current.getUser());
        next.setTokenHash(nextHash);
        next.setExpiresAt(now.plusNanos(toNanos(refreshExpirationMs)));
        refreshTokenRepository.save(next);

        return new RotatedRefreshToken(current.getUser(), nextRawToken);
    }

    public void revokeToken(String rawToken) {
        String tokenHash = hashToken(rawToken);

        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
            if (token.getRevokedAt() == null) {
                token.setRevokedAt(LocalDateTime.now());
                refreshTokenRepository.save(token);
            }
        });
    }

    public int cleanupExpired() {
        return refreshTokenRepository.deleteExpired(LocalDateTime.now());
    }

    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }

    private String generateRawToken() {
        byte[] randomBytes = new byte[64];
        secureRandom.nextBytes(randomBytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private long toNanos(long milliseconds) {
        return milliseconds * 1_000_000;
    }

    public record RotatedRefreshToken(UserAccount user, String refreshToken) {
    }
}
