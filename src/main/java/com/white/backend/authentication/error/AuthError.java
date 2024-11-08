package com.white.backend.authentication.error;

import com.white.backend.shared.base.Error;
import org.springframework.http.HttpStatus;

public enum AuthError implements Error {

    INVALID_USERNAME_OR_PASSWORD(HttpStatus.UNAUTHORIZED, "Invalid username or password"),

    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "Invalid token"),

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Unauthorized");

    AuthError(HttpStatus code, String message) {

        this.code = code;

        this.message = message;

    }

    public final HttpStatus code;

    public final String message;

    @Override
    public HttpStatus getCode() {

        return code;

    }

    @Override
    public String getMessage() {

        return message;

    }
}
