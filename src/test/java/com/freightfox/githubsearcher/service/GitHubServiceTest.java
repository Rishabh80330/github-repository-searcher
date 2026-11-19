
package com.freightfox.githubsearcher.service;

import com.freightfox.githubsearcher.client.GitHubApiClient;
import com.freightfox.githubsearcher.dto.GitHubApiResponse;
import com.freightfox.githubsearcher.dto.GitHubSearchRequest;
import com.freightfox.githubsearcher.entity.GitHubRepo;
import com.freightfox.githubsearcher.repository.GitHubRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

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
        verifyNoInteractions(repository);
    }

    @Test
    void shouldReturnEmptyListWhenGitHubItemsAreNull() {

        when(apiClient.searchRepositories(
                "spring boot", "Java", "stars"
        )).thenReturn(new GitHubApiResponse(null));

        List<GitHubRepo> result = githubService.searchRepositories(
                new GitHubSearchRequest("spring boot", "Java", "stars")
        );

        assertTrue(result.isEmpty());
        verifyNoInteractions(repository);
    }

    @Test
    void shouldReturnEmptyListWhenGitHubSearchHasNoResults() {

        when(apiClient.searchRepositories(
                "spring boot", "Java", "stars"
        )).thenReturn(new GitHubApiResponse(List.of()));

        List<GitHubRepo> result = githubService.searchRepositories(
                new GitHubSearchRequest("spring boot", "Java", "stars")
        );

        assertTrue(result.isEmpty());
        verify(repository).findAllById(List.of());
        verify(repository, never()).upsertRepository(
                anyLong(), anyString(), any(), anyString(),
                any(), anyInt(), anyInt(), any()
        );
    }

    @Test
    void shouldSaveRepositoriesWhenGitHubSearchIsSuccessful() {

        var githubRepo = new GitHubApiResponse.GitHubApiRepository(
                101L,
                "spring-boot-demo",
                "Demo repository",
                new GitHubApiResponse.Owner("developer"),
                "Java",
                500,
                100,
                Instant.parse("2026-11-19T10:00:00Z")
        );

        when(apiClient.searchRepositories(
                "spring boot", "Java", "stars"
        )).thenReturn(new GitHubApiResponse(List.of(githubRepo)));

        when(repository.findAllById(List.of(101L)))
                .thenReturn(List.of(sampleRepo));

        List<GitHubRepo> result = githubService.searchRepositories(
                new GitHubSearchRequest("spring boot", "Java", "stars")
        );

        assertEquals(1, result.size());
        assertEquals("spring-boot-demo", result.getFirst().getName());

        verify(repository).upsertRepository(
                101L,
                "spring-boot-demo",
                "Demo repository",
                "developer",
                "Java",
                500,
                100,
                Instant.parse("2026-11-19T10:00:00Z")
        );
    }

    @Test
    void shouldSkipRepositoriesWithMissingRequiredFields() {

        var invalidRepo = new GitHubApiResponse.GitHubApiRepository(
                102L,
                null,
                "Invalid repository",
                new GitHubApiResponse.Owner("developer"),
                "Java",
                100,
                20,
                Instant.now()
        );

        when(apiClient.searchRepositories(
                "spring boot", "Java", "stars"
        )).thenReturn(new GitHubApiResponse(List.of(invalidRepo)));

        when(repository.findAllById(List.of()))
                .thenReturn(List.of());

        List<GitHubRepo> result = githubService.searchRepositories(
                new GitHubSearchRequest("spring boot", "Java", "stars")
        );

        assertTrue(result.isEmpty());

        verify(repository, never()).upsertRepository(
                anyLong(), anyString(), any(), anyString(),
                any(), anyInt(), anyInt(), any()
        );
    }

    @Test
    void shouldDefaultNullStarsAndForksToZero() {

        var githubRepo = new GitHubApiResponse.GitHubApiRepository(
                103L,
                "demo-repository",
                null,
                new GitHubApiResponse.Owner("developer"),
                "Java",
                null,
                null,
                null
        );

        when(apiClient.searchRepositories(
                "spring boot", "Java", "stars"
        )).thenReturn(new GitHubApiResponse(List.of(githubRepo)));

        when(repository.findAllById(List.of(103L)))
                .thenReturn(List.of(sampleRepo));

        githubService.searchRepositories(
                new GitHubSearchRequest("spring boot", "Java", "stars")
        );

        verify(repository).upsertRepository(
                103L,
                "demo-repository",
                null,
                "developer",
                "Java",
                0,
                0,
                null
        );
    }

    @Test
    void shouldReturnStoredRepositories() {

        when(repository.findAll(
                any(Specification.class),
                any(Sort.class)
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

        verifyNoInteractions(repository);
    }

    @Test
    void shouldRejectNegativeMinimumStars() {

        assertThrows(
                IllegalArgumentException.class,
                () -> githubService.getRepositories(
                        "Java", -10, "stars"
                )
        );

        verifyNoInteractions(repository);
    }

    @Test
    void shouldSortRepositoriesByStarsByDefault() {

        ArgumentCaptor<Sort> sortCaptor =
                ArgumentCaptor.forClass(Sort.class);

        githubService.getRepositories("Java", null, null);

        verify(repository).findAll(
                any(Specification.class),
                sortCaptor.capture()
        );

        assertEquals(
                Sort.Direction.DESC,
                sortCaptor.getValue().getOrderFor("stars").getDirection()
        );
    }

    @Test
    void shouldSortRepositoriesByForks() {

        ArgumentCaptor<Sort> sortCaptor =
                ArgumentCaptor.forClass(Sort.class);

        githubService.getRepositories("Java", 100, "forks");

        verify(repository).findAll(
                any(Specification.class),
                sortCaptor.capture()
        );

        assertEquals(
                Sort.Direction.DESC,
                sortCaptor.getValue().getOrderFor("forks").getDirection()
        );
    }

    @Test
    void shouldSortRepositoriesByUpdatedDate() {

        ArgumentCaptor<Sort> sortCaptor =
                ArgumentCaptor.forClass(Sort.class);

        githubService.getRepositories("Java", 100, "updated");

        verify(repository).findAll(
                any(Specification.class),
                sortCaptor.capture()
        );

        assertEquals(
                Sort.Direction.DESC,
                sortCaptor.getValue()
                        .getOrderFor("lastUpdated")
                        .getDirection()
        );
    }
}
