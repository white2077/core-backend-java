package com.white.backend.shared.exception;

import com.white.backend.shared.base.Error;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class HttpResponseException extends RuntimeException {

    private final HttpStatus statusCode;

    public HttpResponseException(Error error) {

        super(error.getMessage());

        this.statusCode = error.getCode();

    }

}
