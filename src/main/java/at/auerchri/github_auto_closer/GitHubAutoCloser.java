package at.auerchri.github_auto_closer;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class GitHubAutoCloser {
    private String mOAuthToken;
    private String mNamespace;
    private String mRepository;

    private boolean mIncludePullRequests;
    private int mDaysWithInactivity;

    private static final String CLOSE_MESSAGE = "Hey there!\n" +
            "\n" +
            "We're automatically closing this issue since the original poster (or another commenter) hasn't yet responded to the question or request made to them %1$d days ago. We therefore assume that the user has lost interest or resolved the problem on their own. Closed issues that remain inactive for a long period may get automatically locked.\n" +
            "\n" +
            "Don't worry though; if this is in error, let us know with a comment and we'll be happy to reopen the issue.\n" +
            "\n" +
            "Thanks!\n" +
            "\n" +
            "<sub>(Please note that this is an [automated](https://github.com/auchri/github_auto_closer) comment.)</sub>";

    private GitHubClient mGitHubClient;

    public GitHubAutoCloser(String OAuthToken, String namespace, String repository, boolean includePullRequests, int daysWithInactivity) {
        mOAuthToken = OAuthToken;
        mNamespace = namespace;
        mRepository = repository;
        mIncludePullRequests = includePullRequests;
        mDaysWithInactivity = daysWithInactivity;
    }

    public void run() {
        mGitHubClient = new GitHubClient();
        mGitHubClient.setOAuth2Token(mOAuthToken);

        Logger.log(Logger.Level.DEBUG, "Remaining requests: %1$d", mGitHubClient.getRemainingRequests());

        RepositoryId repositoryId = new RepositoryId(mNamespace, mRepository);

        Map<String, String> filter = new HashMap<>();
        filter.put("state", "open");
        filter.put("sort", "updated");
        filter.put("direction", "asc");

        List<Issue> issues;

        try {
            issues = new IssueService(mGitHubClient).getIssues(repositoryId, filter);
        } catch (Exception e) {
            Logger.log(Logger.Level.ERROR, e, "Error loading issues");
            return;
        }

        for (Issue issue : issues) {
            if (!mIncludePullRequests && issue.getPullRequest() != null) {
                continue;
            }

            long difference = ZonedDateTime.now().toInstant().toEpochMilli() - issue.getUpdatedAt().toInstant().toEpochMilli();
            long daysWithoutActivity = TimeUnit.MILLISECONDS.toDays(difference);

            if (daysWithoutActivity >= mDaysWithInactivity) {
                closeIssue(issue, daysWithoutActivity);
            } else {
                break;
            }
        }
    }

    private void closeIssue(Issue issue, long daysWithoutActivity) {
        String message = String.format(CLOSE_MESSAGE, daysWithoutActivity);

        System.out.println(issue.getTitle());
        System.out.println(daysWithoutActivity);
        System.out.println(message);
        System.out.println();
    }
}
