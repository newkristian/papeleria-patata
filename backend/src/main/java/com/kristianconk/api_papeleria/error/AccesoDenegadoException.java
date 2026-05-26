package com.kristianconk.api_papeleria.error;

public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException(final String message) {
        super(message);
    }

    public AccesoDenegadoException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public AccesoDenegadoException(final Throwable cause) {
        super(cause);
    }

}