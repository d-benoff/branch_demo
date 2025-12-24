package com.dbenoff.branch;

import com.dbenoff.branch.service.GithubDataService;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "github.api.url=http://localhost:8089"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WiremockBranchGithubApiIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private WireMockServer wireMockServer;


    @BeforeEach
    void setup() {
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
        WireMock.configureFor("localhost", 8089);
    }

    @AfterEach
    void teardown() {
        wireMockServer.stop();
    }

    @Test
    @Order(1)
    void testGetUser_NotFound() {
        String username = "octocat";
        stubFor(get(urlEqualTo("/users/" + username))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")));

        ResponseEntity<String> response = restTemplate.getForEntity(
                "/users/" + username,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        verify(getRequestedFor(urlEqualTo("/users/" + username)));
    }

    @Test
    @Order(2)
    void testGetUser_BadGateway() {

        String username = "octocat";
        stubFor(get(urlEqualTo("/users/" + username))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")));

        ResponseEntity<String> response = restTemplate.getForEntity(
                "/users/" + username,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);

        verify(getRequestedFor(urlEqualTo("/users/" + username)));
    }

    @Test
    @Order(3)
    void testGetUser_Success() {
        String username = "octocat";
        stubFor(get(urlEqualTo("/users/" + username))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("GithubUserData.json")));

        stubFor(get(urlEqualTo("/users/" + username + "/repos?per_page=100"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("GithubUserRepoData.json")));

        ResponseEntity<String> response = restTemplate.getForEntity(
                "/users/" + username,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().equals(getResourceContent("BranchUserAndRepoData.json")));

        verify(getRequestedFor(urlEqualTo("/users/" + username)));
        verify(getRequestedFor(urlEqualTo("/users/" + username + "/repos?per_page=100")));
    }


    @Test
    @Order(4)
    void testGetUser_ReturnCachedData() {
        // Arrange: Mock GitHub API response
        String username = "octocat";
        stubFor(get(urlEqualTo("/users/" + username))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("GithubUserData.json")));

        stubFor(get(urlEqualTo("/users/" + username + "/repos?per_page=100"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("GithubUserRepoData.json")));

        ResponseEntity<String> response = restTemplate.getForEntity(
                "/users/" + username,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().equals(getResourceContent("BranchUserAndRepoData.json")));

        verify(getRequestedFor(urlEqualTo("/users/" + username)));
        verify(getRequestedFor(urlEqualTo("/users/" + username + "/repos?per_page=100")));

        stubFor(get(urlEqualTo("/users/" + username))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")));

        response = restTemplate.getForEntity(
                "/users/" + username,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().equals(getResourceContent("BranchUserAndRepoData.json")));
    }

    @SpringBootApplication
    static class AppConfiguration {}

    private String getResourceContent(String resourceName) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                throw new RuntimeException("Resource not found: " + resourceName);
            }
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
