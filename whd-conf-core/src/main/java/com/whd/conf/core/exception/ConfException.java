package com.whd.conf.core.exception;

/**
 * conf exception
 *
 * @author hayden 2018-02-01 19:04:52
 */
public class ConfException extends RuntimeException {

    private static final long serialVersionUID = 42L;

    public ConfException(String msg) {
        super(msg);
    }

    public ConfException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public ConfException(Throwable cause) {
        super(cause);
    }

}
