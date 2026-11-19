
package com.freightfox.githubsearcher.client;

import com.freightfox.githubsearcher.dto.GitHubApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GitHubApiClientTest {

    private MockRestServiceServer server;
    private GitHubApiClient apiClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://api.github.com");

        server = MockRestServiceServer.bindTo(builder).build();
        apiClient = new GitHubApiClient(builder.build());
    }

    @Test
    void shouldReturnRepositoriesOnSuccessfulResponse() {
        server.expect(requestTo(
                        "https://api.github.com/search/repositories?q=spring%20boot&sort=stars&order=desc&per_page=30"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        """
                        {"items":[]}
                        """,
                        MediaType.APPLICATION_JSON
                ));

        GitHubApiResponse response =
                apiClient.searchRepositories("spring boot", null, "stars");

        assertThat(response).isNotNull();
        assertThat(response.items()).isEmpty();

        server.verify();
    }

    @Test
    void shouldThrowExceptionWhenGitHubReturnsServerError() {
        server.expect(requestTo(
                        "https://api.github.com/search/repositories?q=spring&sort=stars&order=desc&per_page=30"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() ->
                apiClient.searchRepositories("spring", null, "stars"))
                .isInstanceOf(HttpServerErrorException.class);

        server.verify();
    }

    @Test
    void shouldThrowExceptionWhenGitHubRateLimitIsExceeded() {
        server.expect(requestTo(
                        "https://api.github.com/search/repositories?q=spring&sort=stars&order=desc&per_page=30"))
                .andRespond(withStatus(HttpStatus.FORBIDDEN)
                        .header("X-RateLimit-Remaining", "0")
                        .header("X-RateLimit-Reset", "1795000000"));

        assertThatThrownBy(() ->
                apiClient.searchRepositories("spring", null, "stars"))
                .isInstanceOf(HttpClientErrorException.Forbidden.class);

        server.verify();
    }

    @Test
    void shouldThrowExceptionWhenGitHubConnectionFails() {
        server.expect(requestTo(
                        "https://api.github.com/search/repositories?q=spring&sort=stars&order=desc&per_page=30"))
                .andRespond(request -> {
                    throw new ResourceAccessException("Connection failed");
                });

        assertThatThrownBy(() ->
                apiClient.searchRepositories("spring", null, "stars"))
                .isInstanceOf(ResourceAccessException.class);

        server.verify();
    }
}

