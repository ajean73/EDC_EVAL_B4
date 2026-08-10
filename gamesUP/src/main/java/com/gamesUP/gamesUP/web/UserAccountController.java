package com.gamesUP.gamesUP.web;

import com.gamesUP.gamesUP.domain.UserAccount;
import com.gamesUP.gamesUP.service.UserAccountService;
import com.gamesUP.gamesUP.web.dto.UserAccountRequest;
import com.gamesUP.gamesUP.web.dto.UserAccountResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/identity/users")
public class UserAccountController {

    private final UserAccountService userAccountService;

    public UserAccountController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @GetMapping
    public List<UserAccountResponse> findAll() {
        return userAccountService.findAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public UserAccountResponse findById(@PathVariable UUID id) {
        return toResponse(userAccountService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserAccountResponse create(@RequestBody UserAccountRequest request) {
        UserAccount created = userAccountService.create(toEntity(request));
        return toResponse(created);
    }

    @PutMapping("/{id}")
    public UserAccountResponse update(@PathVariable UUID id, @RequestBody UserAccountRequest request) {
        UserAccount updated = userAccountService.update(id, toEntity(request));
        return toResponse(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        userAccountService.delete(id);
    }

    private UserAccount toEntity(UserAccountRequest request) {
        UserAccount user = new UserAccount();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setPasswordHash(request.passwordHash());
        user.setRole(request.role());
        return user;
    }

    private UserAccountResponse toResponse(UserAccount user) {
        return new UserAccountResponse(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getRole(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
