
package com.freightfox.githubsearcher.repository;

import com.freightfox.githubsearcher.entity.GitHubRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class GitHubRepositoryIntegrationTest {

    @Autowired
    private GitHubRepository repository;

    @Test
    void shouldInsertNewRepository() {
        Instant updatedAt = Instant.parse("2026-11-19T10:00:00Z");

        repository.upsertRepository(
                987654321L,
                "spring-boot-demo",
                "Test repository",
                "test-owner",
                "Java",
                100,
                25,
                updatedAt
        );

        GitHubRepo saved = repository.findById(987654321L).orElseThrow();

        assertThat(saved.getName()).isEqualTo("spring-boot-demo");
        assertThat(saved.getOwner()).isEqualTo("test-owner");
        assertThat(saved.getStars()).isEqualTo(100);
        assertThat(saved.getForks()).isEqualTo(25);
    }

    @Test
    void shouldUpdateExistingRepositoryWithoutCreatingDuplicate() {
        repository.upsertRepository(
                987654322L,
                "old-name",
                "Old description",
                "old-owner",
                "Java",
                50,
                10,
                Instant.parse("2026-11-18T10:00:00Z")
        );

        repository.upsertRepository(
                987654322L,
                "new-name",
                "Updated description",
                "new-owner",
                "Java",
                200,
                40,
                Instant.parse("2026-11-19T10:00:00Z")
        );

        GitHubRepo updated = repository.findById(987654322L).orElseThrow();

        assertThat(updated.getName()).isEqualTo("new-name");
        assertThat(updated.getDescription()).isEqualTo("Updated description");
        assertThat(updated.getOwner()).isEqualTo("new-owner");
        assertThat(updated.getStars()).isEqualTo(200);
        assertThat(updated.getForks()).isEqualTo(40);

        assertThat(repository.count()).isEqualTo(1);
    }
}
