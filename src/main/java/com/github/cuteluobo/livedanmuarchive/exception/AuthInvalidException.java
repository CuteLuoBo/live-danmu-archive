package com.github.cuteluobo.livedanmuarchive.exception;

/**
 * 账户失效错误
 *
 * @author: CuteLuoBo
 * @date: 2025/7/10  17:57
 * @version: 1.0.0
 */
public class AuthInvalidException extends RuntimeException {
    public AuthInvalidException(String message) {
        super(message);
    }
}
