package com.gamesUP.gamesUP.service.impl;

import com.gamesUP.gamesUP.domain.UserAccount;
import com.gamesUP.gamesUP.repository.UserAccountRepository;
import com.gamesUP.gamesUP.service.ResourceNotFoundException;
import com.gamesUP.gamesUP.service.UserAccountService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserAccountServiceImpl implements UserAccountService {

    private final UserAccountRepository userAccountRepository;

    public UserAccountServiceImpl(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
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
        return userAccountRepository.save(userAccount);
    }

    @Override
    public UserAccount update(UUID id, UserAccount userAccount) {
        UserAccount existing = findById(id);
        existing.setFirstName(userAccount.getFirstName());
        existing.setLastName(userAccount.getLastName());
        existing.setEmail(userAccount.getEmail());
        existing.setPasswordHash(userAccount.getPasswordHash());
        existing.setRole(userAccount.getRole());
        return userAccountRepository.save(existing);
    }

    @Override
    public void delete(UUID id) {
        UserAccount existing = findById(id);
        userAccountRepository.delete(existing);
    }
}
