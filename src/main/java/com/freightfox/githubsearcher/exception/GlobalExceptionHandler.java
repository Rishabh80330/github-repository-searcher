
package com.freightfox.githubsearcher.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status,
            String message
    ) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex
    ) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidJson(
            HttpMessageNotReadableException ex
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid JSON request body"
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
    }

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<Map<String, Object>> handleGitHubApiError(
            RestClientResponseException ex
    ) {
        HttpHeaders responseHeaders = new HttpHeaders();
        HttpHeaders githubHeaders = ex.getResponseHeaders();

        String responseBody = ex.getResponseBodyAsString()
                .toLowerCase(Locale.ROOT);

        boolean rateLimited =
                ex.getStatusCode().value() == 429
                        || (ex.getStatusCode().value() == 403
                        && (
                        "0".equals(githubHeaders == null
                                ? null
                                : githubHeaders.getFirst("X-RateLimit-Remaining"))
                                || responseBody.contains("rate limit")
                ));

        if (rateLimited) {
            if (githubHeaders != null) {
                String retryAfter = githubHeaders.getFirst("Retry-After");

                if (retryAfter != null) {
                    responseHeaders.set("Retry-After", retryAfter);
                }
            }

            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .headers(responseHeaders)
                    .body(Map.of(
                            "timestamp", Instant.now().toString(),
                            "status", 429,
                            "error", "Too Many Requests",
                            "message", "GitHub API rate limit exceeded. Please try again later."
                    ));
        }

        return buildResponse(
                HttpStatus.BAD_GATEWAY,
                "GitHub API request failed. Please try again later."
        );
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<Map<String, Object>> handleConnectionError(
            ResourceAccessException ex
    ) {
        return buildResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Unable to connect to GitHub API. Please try again later."
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralError(
            Exception ex
    ) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected server error occurred."
        );
    }
}


