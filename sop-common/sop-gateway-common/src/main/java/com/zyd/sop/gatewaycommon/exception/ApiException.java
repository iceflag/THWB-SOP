package com.zyd.sop.gatewaycommon.exception;


import com.zyd.sop.gatewaycommon.message.Error;

/**
 * @author tanghc
 */
public class ApiException extends RuntimeException {

    private transient final Error error;

    public ApiException(Throwable cause, Error error) {
        super(cause);
        this.error = error;
    }

    public ApiException(Error error) {
        super(error.toString());
        this.error = error;
    }

    public Error getError() {
        return error;
    }
}
