package com.white.backend.shared.exception;

import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(HttpResponseException.class)
    public ResponseEntity<ErrorResponse> handleHttpResponseException(HttpResponseException e) {

        ErrorResponse errorResponse = new ErrorResponse(e.getStatusCode(), e.getMessage());

        return new ResponseEntity<>(errorResponse, e.getStatusCode());

    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ErrorResponse> handlePropertyReferenceException(PropertyReferenceException e) {

        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());

        return new ResponseEntity<>(errorResponse, errorResponse.status());

    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(PropertyReferenceException e) {

        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");

        return new ResponseEntity<>(errorResponse, errorResponse.status());

    }

}
