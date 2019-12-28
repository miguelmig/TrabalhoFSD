package config;

public class Config {
    public static final int ADDR_START = 8000;
    public static final int MAX_PROCESSES = 10;
    public static final int CLIENT_PORT_OFFSET = 1000;
    public static final int LEADER_CHOOSE_TIME = 5; // 5 seconds to choose leader.
    public static final int HEARTBEAT_REPONSE_TIME = 1; // Wait 1 second to get all heartbeat responses.
    public static final int HEARTBEAT_INTERVAL_TIME = 5; // 5 seconds between heartbeat checks

    public static final int TS_DIFF_PROBABLY_RESTART = 20; // We assume that if there are more than 50 messages to be delivered
    // then it's probably a server restart and let's just trust the TS we receive.
    private Config() {}
}