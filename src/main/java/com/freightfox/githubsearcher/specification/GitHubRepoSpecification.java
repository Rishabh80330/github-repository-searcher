
package com.freightfox.githubsearcher.specification;

import com.freightfox.githubsearcher.entity.GitHubRepo;
import org.springframework.data.jpa.domain.Specification;

public class GitHubRepoSpecification {

    public static Specification<GitHubRepo> hasLanguage(String language) {
        return (root, query, cb) ->
                language == null || language.isBlank()
                        ? cb.conjunction()
                        : cb.equal(
                        cb.lower(root.get("language")),
                        language.toLowerCase()
                );
    }

    public static Specification<GitHubRepo> hasMinimumStars(Integer minStars) {
        return (root, query, cb) ->
                minStars == null
                        ? cb.conjunction()
                        : cb.greaterThanOrEqualTo(
                        root.get("stars"), minStars
                );
    }
}

