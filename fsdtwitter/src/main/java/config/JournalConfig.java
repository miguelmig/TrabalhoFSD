package config;

public class JournalConfig {

    private static final String USERS_LOG_NAME = "users_log";
    private static final String POSTS_LOG_NAME = "posts_log";

    public static String getUsersLogName() {
        return USERS_LOG_NAME;
    }

    public static String getPostsLogName() {
        return POSTS_LOG_NAME;
    }
}
