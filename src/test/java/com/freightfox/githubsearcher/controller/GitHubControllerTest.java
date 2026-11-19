
package com.freightfox.githubsearcher.controller;

import com.freightfox.githubsearcher.dto.GitHubSearchRequest;
import com.freightfox.githubsearcher.entity.GitHubRepo;
import com.freightfox.githubsearcher.service.GitHubService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class GitHubControllerTest {

    @Mock
    private GitHubService githubService;

    @InjectMocks
    private GitHubController githubController;

    private MockMvc mockMvc;

    private GitHubRepo sampleRepo;

    @BeforeEach
    void setUp() {

        mockMvc = MockMvcBuilders
                .standaloneSetup(githubController)
                .build();

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
    void shouldSearchRepositoriesSuccessfully() throws Exception {

        when(githubService.searchRepositories(
                any(GitHubSearchRequest.class)
        )).thenReturn(List.of(sampleRepo));

        mockMvc.perform(post("/api/github/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "query": "spring boot",
                                    "language": "Java",
                                    "sort": "stars"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Repositories fetched and saved successfully"))
                .andExpect(jsonPath("$.repositories[0].name")
                        .value("spring-boot-demo"))
                .andExpect(jsonPath("$.repositories[0].stars")
                        .value(500));

        verify(githubService)
                .searchRepositories(any(GitHubSearchRequest.class));
    }

    @Test
    void shouldGetRepositoriesSuccessfully() throws Exception {

        when(githubService.getRepositories(
                null, null, "stars"
        )).thenReturn(List.of(sampleRepo));

        mockMvc.perform(get("/api/github/repositories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repositories.length()")
                        .value(1))
                .andExpect(jsonPath("$.repositories[0].name")
                        .value("spring-boot-demo"));

        verify(githubService)
                .getRepositories(null, null, "stars");
    }

    @Test
    void shouldFilterRepositoriesByLanguageAndStars() throws Exception {

        when(githubService.getRepositories(
                "Java", 100, "stars"
        )).thenReturn(List.of(sampleRepo));

        mockMvc.perform(get("/api/github/repositories")
                        .param("language", "Java")
                        .param("minStars", "100")
                        .param("sort", "stars"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repositories.length()")
                        .value(1))
                .andExpect(jsonPath("$.repositories[0].language")
                        .value("Java"))
                .andExpect(jsonPath("$.repositories[0].stars")
                        .value(500));

        verify(githubService)
                .getRepositories("Java", 100, "stars");
    }
}

