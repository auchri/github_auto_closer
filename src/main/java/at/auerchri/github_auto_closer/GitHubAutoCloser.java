package at.auerchri.github_auto_closer;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Closes github issues based on their inactivity
 */
class GitHubAutoCloser {
    private String mOAuthToken;
    private String mNamespace;
    private String mRepository;

    private boolean mIncludePullRequests;
    private int mDaysWithInactivity;
    private Label mLabelToAdd;

    private static final String CLOSE_MESSAGE = "Hey there!\n" +
            "\n" +
            "We're automatically closing this issue since the original poster (or another commenter) hasn't yet responded to the question or request made to them %1$d days ago. We therefore assume that the user has lost interest or resolved the problem on their own. Closed issues that remain inactive for a long period may get automatically locked.\n" +
            "\n" +
            "Don't worry though; if this is in error, let us know with a comment and we'll be happy to reopen the issue.\n" +
            "\n" +
            "Thanks!\n" +
            "\n" +
            "<sub>(Please note that this is an [automated](https://github.com/auchri/github_auto_closer) comment.)</sub>";

    // Labels for issues which should not be closed
    private static final List<String> LABELS_KEEP = new ArrayList<>();

    static {
        LABELS_KEEP.add("bug");
        LABELS_KEEP.add("enhancement");
    }

    private static final String STATE_OPEN = "open";
    private static final String STATE_CLOSED = "closed";

    GitHubAutoCloser(String OAuthToken, String namespace, String repository, boolean includePullRequests,
                     int daysWithInactivity, String labelToAdd) {
        mOAuthToken = OAuthToken;
        mNamespace = namespace;
        mRepository = repository;
        mIncludePullRequests = includePullRequests;
        mDaysWithInactivity = daysWithInactivity;

        if (labelToAdd != null) {
            mLabelToAdd = new Label().setName(labelToAdd);
        }
    }

    /**
     * Runs the auto closing program
     */
    void run() {
        GitHubClient gitHubClient = new GitHubClient();
        gitHubClient.setOAuth2Token(mOAuthToken);

        Logger.log(Logger.Level.DEBUG, "Remaining requests: %1$d", gitHubClient.getRemainingRequests());

        IssueService issueService = new IssueService(gitHubClient);
        RepositoryId repositoryId = new RepositoryId(mNamespace, mRepository);
        List<Issue> issues = getIssues(issueService, repositoryId);

        if (issues == null) {
            return;
        }

        int nClosedIssues = 0;

        for (Issue issue : issues) {
            if (!mIncludePullRequests && issue.getPullRequest() != null) {
                continue;
            }

            long difference = new Date().getTime() - issue.getUpdatedAt().getTime();
            long daysWithoutActivity = TimeUnit.MILLISECONDS.toDays(difference);

            if (daysWithoutActivity >= mDaysWithInactivity) {
                if (issueHasLabels(issue)) {
                    continue;
                }

                boolean wasError = closeIssue(issueService, repositoryId, issue, daysWithoutActivity);

                if (wasError) {
                    break;
                }

                nClosedIssues++;
            } else {
                break;
            }
        }

        Logger.log(Logger.Level.INFO, "Finished, closed %1$d issues", nClosedIssues);
    }

    /**
     * Returns the issues from the repo
     *
     * @param issueService IssueService
     * @return A list with issues or null, if a loading error occurs
     */
    private List<Issue> getIssues(IssueService issueService, RepositoryId repositoryId) {
        Map<String, String> filter = new HashMap<>();
        filter.put("state", STATE_OPEN);
        filter.put("sort", "updated");
        filter.put("direction", "asc");

        List<Issue> issues = null;

        try {
            issues = issueService.getIssues(repositoryId, filter);
        } catch (Exception e) {
            Logger.log(Logger.Level.ERROR, e, "Error loading issues");
        }

        return issues;
    }

    /**
     * Checks if the issue has "keep" labels (issues which should be not closed)
     *
     * @param issue The issue which should be checked
     * @return True if the issue a one of the keep labels
     */
    private boolean issueHasLabels(Issue issue) {
        for (Label label : issue.getLabels()) {
            for (String keep : LABELS_KEEP) {
                if (label.getName().equals(keep)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Closes an issue
     *
     * @param issueService        The issue service
     * @param repositoryId        The repository of the issue
     * @param issue               The issue which should be closed
     * @param daysWithoutActivity Amount of days without activity
     * @return True if there was an error
     */
    private boolean closeIssue(IssueService issueService, RepositoryId repositoryId, Issue issue, long daysWithoutActivity) {
        String message = String.format(CLOSE_MESSAGE, daysWithoutActivity);

        if (mLabelToAdd != null) {
            issue.getLabels().add(mLabelToAdd);
        }

        issue.setState(STATE_CLOSED);

        try {
            issueService.editIssue(repositoryId, issue);
            issueService.createComment(repositoryId, issue.getNumber(), message);

            Logger.log(Logger.Level.INFO, "Closed issue %1$d (%2$s)", issue.getNumber(), issue.getTitle());
        } catch (Exception e) {
            Logger.log(Logger.Level.ERROR, e, "Error closing issue %1$d", issue.getNumber());
            return true;
        }

        return false;
    }
}
