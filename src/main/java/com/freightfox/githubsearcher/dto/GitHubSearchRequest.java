package com.freightfox.githubsearcher.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record GitHubSearchRequest(

        @NotBlank(message = "Search query is required")
        @Size(max = 256, message = "Query cannot exceed 256 characters")
        String query,

        @Size(max = 50, message = "Language cannot exceed 50 characters")
        String language,

        @Pattern(
                regexp = "(?i)^(stars|forks|updated)$",
                message = "Sort must be stars, forks, or updated"
        )
        String sort

) {}

