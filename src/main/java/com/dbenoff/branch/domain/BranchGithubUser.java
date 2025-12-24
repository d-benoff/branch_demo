package com.dbenoff.branch.domain;

import java.util.List;

public record BranchGithubUser(
        String user_name,
        String display_name,
        String avatar,
        String geo_location,
        String email,
        String url,
        String created_at,
        List<Repo> repos
) {
    public record Repo(String name, String url) {}
}
