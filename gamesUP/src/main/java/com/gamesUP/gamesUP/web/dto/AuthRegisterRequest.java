package com.gamesUP.gamesUP.web.dto;

public record AuthRegisterRequest(
    String firstName,
    String lastName,
    String email,
    String password
) {
}
