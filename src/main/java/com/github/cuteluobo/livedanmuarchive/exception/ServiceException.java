package com.github.cuteluobo.livedanmuarchive.exception;

/**
 * 服务异常
 * @author CuteLuoBo
 * @date 2021/12/18 11:49
 */
public class ServiceException extends Exception {
    private Exception originalException;
    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Exception originalException) {
        super(message);
        this.originalException = originalException;
    }

    public Exception getOriginalException() {
        return originalException;
    }

    public void setOriginalException(Exception originalException) {
        this.originalException = originalException;
    }
}
