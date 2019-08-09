package com.zyd.sop.servercommon.exception;

import com.zyd.sop.servercommon.message.ServiceError;
import com.zyd.sop.servercommon.message.ServiceErrorEnum;
import com.zyd.sop.servercommon.message.ServiceErrorImpl;

/**
 * @author tanghc
 */
public class ServiceException extends RuntimeException {

    private final transient ServiceError error;

    public ServiceException(String subCode, String subMsg) {
        super(subMsg);
        this.error = new ServiceErrorImpl(subCode, subMsg);
    }

    public ServiceException(String subMsg) {
        super(subMsg);
        String subCode = ServiceErrorEnum.ISV_COMMON_ERROR.getErrorMeta().getError().getSub_code();
        this.error = new ServiceErrorImpl(subCode, subMsg);
    }

    public ServiceException(ServiceError error) {
        super(error.toString());
        this.error = error;
    }

    public ServiceError getError() {
        return error;
    }
}
