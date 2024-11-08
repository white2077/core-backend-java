package com.white.backend.shared.base;

import com.white.backend.shared.exception.HttpResponseException;
import org.springframework.http.HttpStatus;

public interface Error {

    HttpStatus getCode();

    String getMessage();

    default HttpResponseException exception() {
        return new HttpResponseException(this);
    }

}
