package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.domain.UserAccount;
import java.util.List;
import java.util.UUID;

public interface UserAccountService {
    List<UserAccount> findAll();

    UserAccount findById(UUID id);

    UserAccount create(UserAccount userAccount);

    UserAccount update(UUID id, UserAccount userAccount);

    void delete(UUID id);
}
