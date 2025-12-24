package com.dbenoff.branch.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class GithubConnectivityException extends RuntimeException {
    public GithubConnectivityException(Throwable cause) {
        super("Could not connect to Github", cause);
    }
}