package at.auerchri.github_auto_closer;

import org.apache.commons.cli.*;

public class Main {
    private static final String OPTION_DEBUG_SHORT = "x";
    private static final String OPTION_DEBUG_LONG = "debug";

    private static final String OPTION_OAUTH_TOKEN_SHORT = "o";
    private static final String OPTION_OAUTH_TOKEN_LONG = "oauth_token";

    private static final String OPTION_NAMESPACE_SHORT = "n";
    private static final String OPTION_NAMESPACE_LONG = "namespace";

    private static final String OPTION_REPOSITORY_SHORT = "r";
    private static final String OPTION_REPOSITORY_LONG = "repository";

    private static final String OPTION_DAYS_INACTIVITY_SHORT = "d";
    private static final String OPTION_DAYS_INACTIVITY_LONG = "days";

    private static final String OPTION_INCLUDE_PR_SHORT = "p";
    private static final String OPTION_INCLUDE_PR_LONG = "include_pr";

    public static void main(String[] args) {

        Options options = new Options();
        options.addOption(OPTION_DEBUG_SHORT, OPTION_DEBUG_LONG, false, "Enable debug");
        options.addOption(OPTION_OAUTH_TOKEN_SHORT, OPTION_OAUTH_TOKEN_LONG, true,
                "OAuth token from https://github.com/settings/tokens");
        options.addOption(OPTION_NAMESPACE_SHORT, OPTION_NAMESPACE_LONG, true,
                "Namespace of the repository (e.g. foo/bar -> foo");
        options.addOption(OPTION_REPOSITORY_SHORT, OPTION_REPOSITORY_LONG, true,
                "Name of the repository in the namespace (e.g. foo/bar -> bar)");
        options.addOption(OPTION_DAYS_INACTIVITY_SHORT, OPTION_DAYS_INACTIVITY_LONG, true,
                "Days with inactivity");
        options.addOption(OPTION_INCLUDE_PR_SHORT, OPTION_INCLUDE_PR_LONG, false,
                "Include pull requests");

        CommandLineParser commandLineParser = new DefaultParser();
        CommandLine cmd = null;

        try {
            cmd = commandLineParser.parse(options, args);
        } catch (ParseException e) {
            Logger.log(Logger.Level.ERROR, e, "Error parsing command line arguments");
        }

        if (cmd == null) {
            return;
        }

        if (cmd.hasOption(OPTION_DEBUG_SHORT)) {
            Logger.setMinLogLevel(Logger.Level.DEBUG);
            Logger.log(Logger.Level.WARN, "Enabled debug mode");
        }

        if (!checkCmdParameter(cmd, OPTION_OAUTH_TOKEN_SHORT, OPTION_OAUTH_TOKEN_LONG)) {
            return;
        }

        if (!checkCmdParameter(cmd, OPTION_NAMESPACE_SHORT, OPTION_NAMESPACE_LONG)) {
            return;
        }

        if (!checkCmdParameter(cmd, OPTION_REPOSITORY_SHORT, OPTION_REPOSITORY_LONG)) {
            return;
        }

        if (!checkCmdParameter(cmd, OPTION_DAYS_INACTIVITY_SHORT, OPTION_DAYS_INACTIVITY_LONG)) {
            return;
        }

        Integer dayResult = getIntParameter(cmd, OPTION_DAYS_INACTIVITY_SHORT);

        if (dayResult == null) {
            Logger.log(Logger.Level.ERROR, "The value for the inactivity days is not an integer");
            return;
        }

        String oAuthToken = cmd.getOptionValue(OPTION_OAUTH_TOKEN_SHORT);
        String namespace = cmd.getOptionValue(OPTION_NAMESPACE_SHORT);
        String repository = cmd.getOptionValue(OPTION_REPOSITORY_SHORT);
        int days = dayResult;
        boolean includePRs = cmd.hasOption(OPTION_INCLUDE_PR_SHORT);

        new GitHubAutoCloser(oAuthToken, namespace, repository, includePRs, days).run();
    }

    private static boolean checkCmdParameter(CommandLine cmd, String s, String l) {
        if (cmd.getOptionValue(s) == null) {
            Logger.log(Logger.Level.ERROR, String.format("Required %1$s is missing from the parameters", l));
            return false;
        }

        return true;
    }

    private static Integer getIntParameter(CommandLine cmd, String shortName) {
        String value = cmd.getOptionValue(shortName);
        Integer result = null;

        try {
            result = Integer.valueOf(value);
        } catch (Exception e) {
            Logger.log(Logger.Level.DEBUG, e, "Error parsing %1$s", shortName);
        }

        return result;
    }
}
