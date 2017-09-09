package project.chronos.timewalk;

/**
 * Created by Fletcher on 9/09/2017.
 */

public class TransferCode {

    //  **  Request Codes   **
    public static final int LIST_REGIONS = 0;
    public static final int LIST_LANDMARKS = 1;
    public static final int LIST_IMAGES = 2;
    public static final int GET_REGION_GPS = 10;
    public static final int GET_LANDMARK_GPS = 11;
    public static final int GET_TEXT = 20;
    public static final int GET_IMAGE = 21;
    public static final int GET_POSTCARD_IMAGE_LANDMARK = 30;
    public static final int GET_POSTCARD_IMAGE_REGION = 31;
    // ...

    //  ** Transfer Codes  **
    public static final int SUCESS = 0;

    // ...

    //  ** Error Codes **
    public static final int NETWORK_ERROR = -10;
    public static final int ASYNC_THREAD_ERROR = -11;
    // ...
}
