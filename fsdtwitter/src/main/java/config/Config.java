package config;

public class Config {
    public static final int ADDR_START = 8000;
    public static final int MAX_PROCESSES = 10;
    public static final int CLIENT_PORT_OFFSET = 1000;
    public static final int LEADER_CHOOSE_TIME = 5; // 5 seconds to choose leader.

    private Config() {}
}