package at.auerchri.github_auto_closer;

class Logger {
    private static Level sMinLogLevel = Level.INFO;

    static void setMinLogLevel(Level minLogLevel) {
        sMinLogLevel = minLogLevel;
    }

    enum Level {
        DEBUG(10), INFO(20), ERROR(30);

        private final int mNum;

        Level(int num) {
            mNum = num;
        }

        /**
         * Checks if the current level is lower than the given
         *
         * @param second The second level
         * @return True if the level is lower than the given one
         */
        boolean isLowerThan(Level second) {
            return mNum < second.mNum;
        }
    }

    static void log(Level level, String format, Object... args) {
        log(level, String.format(format, args));
    }

    static void log(Level level, String message) {
        log(level, null, message);
    }

    static void log(Level level, Throwable e, String format, Object... args) {
        log(level, e, String.format(format, args));
    }

    static void log(Level level, Throwable e, String message) {
        if (level.isLowerThan(sMinLogLevel)) {
            return;
        }

        if (e != null) {
            printMessage(level, String.format("%1$s: %2$s\n", message, getThrowableMessage(e)));

            if (sMinLogLevel == Level.DEBUG) {
                e.printStackTrace();
            }
        } else {
            printMessage(level, message);
        }
    }

    private static void printMessage(Level level, String message) {
        System.out.printf("[%1$s] %2$s\n", level.toString(), message);
    }

    private static String getThrowableMessage(Throwable throwable) {
        // TODO Get message from GitHub exception
        return throwable.getMessage();
    }
}
