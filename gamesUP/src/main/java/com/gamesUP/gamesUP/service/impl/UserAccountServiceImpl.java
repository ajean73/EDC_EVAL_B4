package com.gamesUP.gamesUP.service.impl;

import com.gamesUP.gamesUP.domain.UserAccount;
import com.gamesUP.gamesUP.domain.UserRole;
import com.gamesUP.gamesUP.repository.UserAccountRepository;
import com.gamesUP.gamesUP.service.ResourceNotFoundException;
import com.gamesUP.gamesUP.service.UserAccountService;
import java.util.List;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserAccountServiceImpl implements UserAccountService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAccountServiceImpl(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserAccount> findAll() {
        return userAccountRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public UserAccount findById(UUID id) {
        return userAccountRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    @Override
    public UserAccount create(UserAccount userAccount) {
        userAccount.setPasswordHash(encodeIfNeeded(userAccount.getPasswordHash()));
        if (userAccount.getRole() == null) {
            userAccount.setRole(UserRole.CUSTOMER);
        }
        return userAccountRepository.save(userAccount);
    }

    @Override
    public UserAccount update(UUID id, UserAccount userAccount) {
        UserAccount existing = findById(id);
        existing.setFirstName(userAccount.getFirstName());
        existing.setLastName(userAccount.getLastName());
        existing.setEmail(userAccount.getEmail());
        existing.setPasswordHash(encodeIfNeeded(userAccount.getPasswordHash()));
        existing.setRole(userAccount.getRole() != null ? userAccount.getRole() : existing.getRole());
        return userAccountRepository.save(existing);
    }

    @Override
    public void delete(UUID id) {
        UserAccount existing = findById(id);
        userAccountRepository.delete(existing);
    }

    private String encodeIfNeeded(String rawOrEncodedPassword) {
        if (rawOrEncodedPassword == null || rawOrEncodedPassword.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        if (rawOrEncodedPassword.startsWith("$2a$")
            || rawOrEncodedPassword.startsWith("$2b$")
            || rawOrEncodedPassword.startsWith("$2y$")) {
            return rawOrEncodedPassword;
        }

        return passwordEncoder.encode(rawOrEncodedPassword);
    }
}
