package com.github.sgwhp.openapm.sample.logging;

/**
 * Created by user on 2016/8/1.
 */
public interface AgentLog {
    public static final int DEBUG = 5;
    public static final int VERBOSE = 4;
    public static final int INFO = 3;
    public static final int WARNING = 2;
    public static final int ERROR = 1;

    void debug(String p0);
    void verbose(String p0);
    void info(String p0);
    void warning(String p0);
    void error(String p0);
    void error(String p0, Throwable p1);
    int getLevel();
    void setLevel(int p0);
}
