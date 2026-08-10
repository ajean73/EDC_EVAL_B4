package com.gamesUP.gamesUP.web;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gamesUP.gamesUP.service.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void handlesNotFound() {
        var response = handler.handleNotFound(new ResourceNotFoundException("missing"));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("missing", response.getBody().message());
    }

    @Test
    void handlesBadRequest() {
        var response = handler.handleBadRequest(new IllegalArgumentException("bad"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handlesConflict() {
        var response = handler.handleConflict(new IllegalStateException("conflict"));
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void handlesAuthenticationError() {
        var response = handler.handleAuthenticationError(new BadCredentialsException("bad"));
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid credentials", response.getBody().message());
    }

    @Test
    void handlesForbidden() {
        var response = handler.handleAccessDenied(new AccessDeniedException("forbidden"));
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
}
