
package com.freightfox.githubsearcher.service;

import com.freightfox.githubsearcher.client.GitHubApiClient;
import com.freightfox.githubsearcher.dto.GitHubApiResponse;
import com.freightfox.githubsearcher.dto.GitHubSearchRequest;
import com.freightfox.githubsearcher.entity.GitHubRepo;
import com.freightfox.githubsearcher.repository.GitHubRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GitHubServiceTest {

    @Mock
    private GitHubApiClient apiClient;

    @Mock
    private GitHubRepository repository;

    @InjectMocks
    private GitHubService githubService;

    private GitHubRepo sampleRepo;

    @BeforeEach
    void setUp() {
        sampleRepo = new GitHubRepo();

        sampleRepo.setId(101L);
        sampleRepo.setName("spring-boot-demo");
        sampleRepo.setOwner("developer");
        sampleRepo.setLanguage("Java");
        sampleRepo.setStars(500);
        sampleRepo.setForks(100);
        sampleRepo.setLastUpdated(Instant.now());
    }

    @Test
    void shouldReturnEmptyListWhenGitHubResponseIsNull() {

        when(apiClient.searchRepositories(
                "spring boot", "Java", "stars"
        )).thenReturn(null);

        List<GitHubRepo> result = githubService.searchRepositories(
                new GitHubSearchRequest("spring boot", "Java", "stars")
        );

        assertTrue(result.isEmpty());

        verify(repository, never()).upsertRepository(
                anyLong(),
                anyString(),
                any(),
                anyString(),
                any(),
                anyInt(),
                anyInt(),
                any()
        );
    }

    @Test
    void shouldReturnStoredRepositories() {

        when(repository.findAll(
                any(org.springframework.data.jpa.domain.Specification.class),
                any(org.springframework.data.domain.Sort.class)
        )).thenReturn(List.of(sampleRepo));

        List<GitHubRepo> result =
                githubService.getRepositories("Java", 100, "stars");

        assertEquals(1, result.size());
        assertEquals("spring-boot-demo", result.getFirst().getName());
        assertEquals(500, result.getFirst().getStars());
    }

    @Test
    void shouldRejectInvalidSort() {

        assertThrows(
                IllegalArgumentException.class,
                () -> githubService.getRepositories(
                        "Java", 100, "invalid"
                )
        );

        verify(repository, never()).findAll(
                any(org.springframework.data.jpa.domain.Specification.class),
                any(org.springframework.data.domain.Sort.class)
        );
    }

    @Test
    void shouldRejectNegativeMinimumStars() {

        assertThrows(
                IllegalArgumentException.class,
                () -> githubService.getRepositories(
                        "Java", -10, "stars"
                )
        );
    }
}

