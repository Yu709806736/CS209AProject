package util;

public class FailureResponse extends Response {


    public FailureResponse(int code, String message) {
        super(code, message);
    }

    public FailureResponse(FailureCause failureCause){
        super(failureCause.code, failureCause.message);
    }

}

