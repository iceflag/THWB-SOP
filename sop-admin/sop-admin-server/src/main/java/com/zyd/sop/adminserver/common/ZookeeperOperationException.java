package com.zyd.sop.adminserver.common;

/**
 * @author tanghc
 */
public class ZookeeperOperationException extends RuntimeException {

    public ZookeeperOperationException() {
    }

    public ZookeeperOperationException(String message) {
        super(message);
    }

    public ZookeeperOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZookeeperOperationException(Throwable cause) {
        super(cause);
    }

    public ZookeeperOperationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
