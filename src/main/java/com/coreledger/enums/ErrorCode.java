package com.coreledger.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    USER_NOT_FOUND(1001, "user not found", HttpStatus.NOT_FOUND),
    USERNAME_ALREADY_EXISTS(1002, "username already exists", HttpStatus.CONFLICT),
    INVALID_CREDENTIALS(1003, "invalid credentials", HttpStatus.UNAUTHORIZED),
    ACCOUNT_NOT_FOUND(2001, "account not found", HttpStatus.NOT_FOUND),
    ACCOUNT_DISABLED(2002, "account disabled", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_BALANCE(2003, "insufficient balance", HttpStatus.BAD_REQUEST),
    DUPLICATE_REQUEST(3001, "duplicate request", HttpStatus.CONFLICT),
    OPTIMISTIC_LOCK_CONFLICT(3002, "account balance was modified concurrently", HttpStatus.CONFLICT),
    UNAUTHORIZED(4001, "unauthorized", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(4003, "forbidden", HttpStatus.FORBIDDEN),
    INVALID_PARAMETER(4004, "invalid parameter", HttpStatus.BAD_REQUEST),
    INTERNAL_ERROR(5000, "internal error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
