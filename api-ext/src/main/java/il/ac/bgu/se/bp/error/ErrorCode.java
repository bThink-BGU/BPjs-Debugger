package il.ac.bgu.se.bp.error;

public enum ErrorCode {
    BP_SETUP_FAIL(0),
    SETUP_REQUIRED(1),
    NOT_STARTED(2),
    NOT_RUNNING(3),
    ALREADY_RUNNING(4),
    NOT_IN_JS_DEBUG_STATE(5),
    NOT_IN_BP_SYNC_STATE(6),
    CANNOT_REPLACE_SNAPSHOT(7),


    WAITING_FOR_EXTERNAL_EVENT(20),



    BREAKPOINT_NOT_ALLOWED(30), // todo: add lineNumber



    FAILED_ADDING_COMMAND(40),
    ;



    private final int errorCode;

    ErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }



}
