package project.chronos.timewalk;

/**
 * Created by Fletcher on 11/09/2017.
 */

public class ResultCode {
    //  Transfer Codes must EXACTLY match server codes
    public static final int SUCCESS = 0;

    //  Errors
    public static final int DISCONNECTED = -1;
    public static final int THREAD_FAILURE = -2;
    public static final int SERVER_DOWN = -3;
    public static final int CONNECTION_FAILED = -4;
    public static final int THREAD_TIMEOUT = -5;
    public static final int BAD_READ = -6;
}
