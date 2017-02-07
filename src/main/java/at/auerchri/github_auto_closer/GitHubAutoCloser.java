package at.auerchri.github_auto_closer;

import org.eclipse.egit.github.core.client.GitHubClient;

public class GitHubAutoCloser {
    private String mOAuthToken;
    private String mNamespace;
    private String mRepository;

    private GitHubClient mGitHubClient;

    public GitHubAutoCloser(String OAuthToken, String namespace, String repository) {
        mOAuthToken = OAuthToken;
        mNamespace = namespace;
        mRepository = repository;
    }

    public void run() {
        mGitHubClient = new GitHubClient();
        mGitHubClient.setOAuth2Token(mOAuthToken);
    }
}
