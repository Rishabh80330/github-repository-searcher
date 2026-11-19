
package com.freightfox.githubsearcher.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;

public record GitHubApiResponse(
        List<GitHubApiRepository> items
) {
    public record GitHubApiRepository(
            Long id,
            String name,
            String description,
            Owner owner,
            String language,

            @JsonProperty("stargazers_count")
            Integer stars,

            @JsonProperty("forks_count")
            Integer forks,

            @JsonProperty("updated_at")
            Instant lastUpdated
    ) {}

    public record Owner(String login) {}
}

