package com.dbenoff.branch.service;

import com.dbenoff.branch.domain.BranchGithubUser;
import com.dbenoff.branch.domain.exception.GithubConnectivityException;
import com.dbenoff.branch.domain.exception.GithubUserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.TimeZone;

@Slf4j
@Service
public class GithubDataService implements InitializingBean {

    @Autowired
    private GithubConnectionService githubConnectionService;
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");;
    private final CacheManager cacheManager;
    private Cache exceptionCache;

    public GithubDataService(CacheManager cacheManager){
        this.cacheManager = cacheManager;
    }

    public BranchGithubUser getUser(String userName) {

        BranchGithubUser branchGithubUser;

        try{
            branchGithubUser =  getUserFromRemote(userName);
            exceptionCache.put(userName, branchGithubUser);
        } catch (IOException ioe){
            branchGithubUser =  exceptionCache.get(userName, BranchGithubUser.class);
            if(branchGithubUser == null)
                throw new GithubConnectivityException(ioe);
        }

        return branchGithubUser;
    }

    public BranchGithubUser getUserFromRemote(String userName) throws IOException {
        GitHub gitHub = githubConnectionService.getGithubConnection();
        GHUser ghUser;
        try{
            ghUser = gitHub.getUser(userName);
        } catch (GHFileNotFoundException gfne){
            log.debug(userName + " not found");
            throw new GithubUserNotFoundException(userName);
        }

        Map<String, GHRepository> repoMap = ghUser.getRepositories();
        return new BranchGithubUser(
                ghUser.getLogin(),
                ghUser.getName(),
                ghUser.getAvatarUrl(),
                ghUser.getLocation(),
                ghUser.getEmail(),
                ghUser.getUrl().toExternalForm(),
                simpleDateFormat.format(ghUser.getCreatedAt()),
                repoMap.keySet().stream()
                        .map(repoName ->
                                new BranchGithubUser.Repo(repoName, repoMap.get(repoName).getUrl().toExternalForm())).toList()
        );
    }

    @Override
    public void afterPropertiesSet() {

        exceptionCache = cacheManager.getCache("exceptionCache");
        if (exceptionCache == null) {
            log.error("Cache not available");
            throw new IllegalStateException("Cache 'exceptionCache' not found");
        }

        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }
}
