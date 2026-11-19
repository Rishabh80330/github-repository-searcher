
package com.freightfox.githubsearcher.controller;

import com.freightfox.githubsearcher.dto.GitHubSearchRequest;
import com.freightfox.githubsearcher.entity.GitHubRepo;
import com.freightfox.githubsearcher.service.GitHubService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/github")
public class GitHubController {

    private final GitHubService githubService;

    public GitHubController(GitHubService githubService) {
        this.githubService = githubService;
    }

    @PostMapping("/search")
    public ResponseEntity<Map<String, Object>> searchRepositories(
            @Valid @RequestBody GitHubSearchRequest request
    ) {

        List<GitHubRepo> repositories =
                githubService.searchRepositories(request);

        return ResponseEntity.ok(Map.of(
                "message", "Repositories fetched and saved successfully",
                "repositories", repositories
        ));
    }

    @GetMapping("/repositories")
    public ResponseEntity<Map<String, Object>> getRepositories(

            @RequestParam(name = "language", required = false)
            String language,

            @RequestParam(name = "minStars", required = false)
            Integer minStars,

            @RequestParam(name = "sort", defaultValue = "stars")
            String sort

    ) {

        List<GitHubRepo> repositories =
                githubService.getRepositories(
                        language,
                        minStars,
                        sort
                );

        return ResponseEntity.ok(Map.of(
                "repositories", repositories
        ));
    }
}

