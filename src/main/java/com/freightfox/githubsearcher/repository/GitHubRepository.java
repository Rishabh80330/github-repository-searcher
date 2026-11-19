
package com.freightfox.githubsearcher.repository;

import com.freightfox.githubsearcher.entity.GitHubRepo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface GitHubRepository
        extends JpaRepository<GitHubRepo, Long>,
        JpaSpecificationExecutor<GitHubRepo> {

    @Modifying
    @Query(value = """
            INSERT INTO github_repositories (
                github_id,
                name,
                description,
                owner,
                language,
                stars,
                forks,
                last_updated
            )
            VALUES (
                :id,
                :name,
                :description,
                :owner,
                :language,
                :stars,
                :forks,
                :lastUpdated
            )
            ON CONFLICT (github_id)
            DO UPDATE SET
                name = EXCLUDED.name,
                description = EXCLUDED.description,
                owner = EXCLUDED.owner,
                language = EXCLUDED.language,
                stars = EXCLUDED.stars,
                forks = EXCLUDED.forks,
                last_updated = EXCLUDED.last_updated
            """, nativeQuery = true)
    int upsertRepository(
            @Param("id") Long id,
            @Param("name") String name,
            @Param("description") String description,
            @Param("owner") String owner,
            @Param("language") String language,
            @Param("stars") Integer stars,
            @Param("forks") Integer forks,
            @Param("lastUpdated") Instant lastUpdated
    );
}


