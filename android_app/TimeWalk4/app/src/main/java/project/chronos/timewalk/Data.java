package project.chronos.timewalk;

/**
 * Created by Fletcher on 11/09/2017.
 */

public class Data<ResultType> {
    ResultType result = null;
    boolean failed = false;
    String errorMsg = null;
    int resultCode = ResultCode.SUCCESS;
}