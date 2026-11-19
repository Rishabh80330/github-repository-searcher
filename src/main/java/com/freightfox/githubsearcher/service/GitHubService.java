
package com.freightfox.githubsearcher.service;

import com.freightfox.githubsearcher.client.GitHubApiClient;
import com.freightfox.githubsearcher.dto.GitHubApiResponse;
import com.freightfox.githubsearcher.dto.GitHubSearchRequest;
import com.freightfox.githubsearcher.entity.GitHubRepo;
import com.freightfox.githubsearcher.repository.GitHubRepository;
import com.freightfox.githubsearcher.specification.GitHubRepoSpecification;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class GitHubService {

    private final GitHubApiClient apiClient;
    private final GitHubRepository repository;

    public GitHubService(
            GitHubApiClient apiClient,
            GitHubRepository repository
    ) {
        this.apiClient = apiClient;
        this.repository = repository;
    }

    @Transactional
    public List<GitHubRepo> searchRepositories(
            GitHubSearchRequest request
    ) {

        String sort = request.sort() == null
                || request.sort().isBlank()
                ? "stars"
                : request.sort().strip().toLowerCase(Locale.ROOT);

        GitHubApiResponse response = apiClient.searchRepositories(
                request.query().trim(),
                request.language(),
                sort
        );

        if (response == null || response.items() == null) {
            return List.of();
        }

        for (var repo : response.items()) {

            if (repo.id() == null
                    || repo.name() == null
                    || repo.owner() == null
                    || repo.owner().login() == null) {
                continue;
            }

            repository.upsertRepository(
                    repo.id(),
                    repo.name(),
                    repo.description(),
                    repo.owner().login(),
                    repo.language(),
                    repo.stars() == null ? 0 : repo.stars(),
                    repo.forks() == null ? 0 : repo.forks(),
                    repo.lastUpdated()
            );
        }

        return repository.findAllById(
                response.items().stream()
                        .filter(repo ->
                                repo.id() != null
                                        && repo.name() != null
                                        && repo.owner() != null
                                        && repo.owner().login() != null
                        )
                        .map(GitHubApiResponse.GitHubApiRepository::id)
                        .toList()
        );
    }

    @Transactional(readOnly = true)
    public List<GitHubRepo> getRepositories(
            String language,
            Integer minStars,
            String sort
    ) {

        String sortField = (sort == null || sort.isBlank())
                ? "stars"
                : sort.strip().toLowerCase(Locale.ROOT);

        if (!List.of("stars", "forks", "updated").contains(sortField)) {
            throw new IllegalArgumentException(
                    "Sort must be stars, forks, or updated"
            );
        }

        if (minStars != null && minStars < 0) {
            throw new IllegalArgumentException(
                    "Minimum stars cannot be negative"
            );
        }

        String sortProperty = switch (sortField) {
            case "stars" -> "stars";
            case "forks" -> "forks";
            case "updated" -> "lastUpdated";
            default -> throw new IllegalStateException("Invalid sort field");
        };

        Sort sorting = Sort.by(
                Sort.Direction.DESC,
                sortProperty
        );

        return repository.findAll(
                GitHubRepoSpecification.hasLanguage(language)
                        .and(GitHubRepoSpecification.hasMinimumStars(minStars)),
                sorting
        );
    }
}
