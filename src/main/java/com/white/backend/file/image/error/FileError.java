package com.white.backend.file.image.error;

import com.white.backend.shared.base.Error;
import org.springframework.http.HttpStatus;

public enum FileError implements Error {

    HAVE_ERROR_WHILE_UPLOADING_FILE(HttpStatus.INTERNAL_SERVER_ERROR, "Have error while uploading file"),

    HAVE_ERROR_WHILE_DELETING_FILE(HttpStatus.INTERNAL_SERVER_ERROR, "Have error while deleting file"),

    FILE_IS_NOT_IMAGE(HttpStatus.BAD_REQUEST, "File is not an image");

    FileError(HttpStatus code, String message) {

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
