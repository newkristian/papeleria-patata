package com.kristianconk.api_papeleria.error;

public class ConflictException extends RuntimeException {

    public ConflictException(final String message) {
        super(message);
    }
}

