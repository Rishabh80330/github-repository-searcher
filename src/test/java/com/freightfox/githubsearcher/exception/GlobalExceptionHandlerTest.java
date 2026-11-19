
package com.freightfox.githubsearcher.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void shouldReturn429WhenGitHubPrimaryRateLimitExceeded() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-RateLimit-Remaining", "0");

        var exception = HttpClientErrorException.create(
                HttpStatus.FORBIDDEN,
                "Forbidden",
                headers,
                "API rate limit exceeded".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );

        ResponseEntity<?> response =
                handler.handleGitHubApiError(exception);

        assertEquals(429, response.getStatusCode().value());
        assertEquals(
                "Too Many Requests",
                response.getBody() instanceof java.util.Map<?, ?> body
                        ? body.get("error") : null
        );
    }

    @Test
    void shouldReturn429WhenGitHubReturns429() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Retry-After", "60");

        var exception = HttpClientErrorException.create(
                HttpStatus.TOO_MANY_REQUESTS,
                "Too Many Requests",
                headers,
                "Rate limit exceeded".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );

        ResponseEntity<?> response =
                handler.handleGitHubApiError(exception);

        assertEquals(429, response.getStatusCode().value());
        assertEquals("60", response.getHeaders().getFirst("Retry-After"));
    }

    @Test
    void shouldReturn502ForNormalGitHub403Error() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-RateLimit-Remaining", "100");

        var exception = HttpClientErrorException.create(
                HttpStatus.FORBIDDEN,
                "Forbidden",
                headers,
                "Access denied".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );

        ResponseEntity<?> response =
                handler.handleGitHubApiError(exception);

        assertEquals(502, response.getStatusCode().value());
    }
}

