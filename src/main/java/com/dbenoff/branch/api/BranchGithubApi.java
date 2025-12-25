package com.dbenoff.branch.api;

import com.dbenoff.branch.domain.BranchGithubUser;
import com.dbenoff.branch.service.GithubDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class BranchGithubApi {

    @Autowired
    private GithubDataService githubDataService;

    @Operation(summary = "Get a user and repo information by Github username", description = "Returns a user by username")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Not found - The user was not found"),
            @ApiResponse(responseCode = "502", description = "Github not reachable")
    })
    @GetMapping(path = "/users/{userName}", produces = "application/json")
    BranchGithubUser getUserByUserName(@PathVariable String userName) {
         return githubDataService.getUser(userName);
    }


}
