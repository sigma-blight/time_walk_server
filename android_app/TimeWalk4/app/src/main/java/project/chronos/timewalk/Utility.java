package project.chronos.timewalk;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Fletcher on 11/09/2017.
 */

public class Utility {

    public static void toastMessage(String message, Context context) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void toastResultCodeError(int resultCode, Context context) {
        switch (resultCode) {
            case ResultCode.DISCONNECTED:
                toastMessage("Lost Connection", context);
                break;

            case ResultCode.SERVER_DOWN:
                toastMessage("Server is down, try again later", context);
                break;

            case ResultCode.THREAD_TIMEOUT:
                toastMessage("Connection timed out", context);
                break;

            case ResultCode.BAD_READ:
                toastMessage("Failed to read from server", context);
                break;

            default:
                toastMessage("Oops! Unknown error occurred", context);
                break;
        }
    }

    public static String displayName(String name) {
        return name.replace('_', ' ');
    }
}
