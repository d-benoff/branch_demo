package com.dbenoff.branch.service;

import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class GithubConnectionService {

    @Value("${github.api.url:#{null}}")
    private String githubEndpoint;


    public GitHub getGithubConnection(){
        try {
            GitHub gitHub = githubEndpoint != null ?
                    new GitHubBuilder().withEndpoint(githubEndpoint).build() : new GitHubBuilder().build();
            log.debug("Established unauthenticated connection to Github");
            return gitHub;
        } catch (IOException e) {
            log.warn("Failed to connect to Github {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
