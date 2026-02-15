package com.kristianconk.api_papeleria.error;

class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException(String message) {
        super(message);
    }

    public AccesoDenegadoException(String message, Throwable cause) {
        super(message, cause);
    }

    public AccesoDenegadoException(Throwable cause) {
        super(cause);
    }

}