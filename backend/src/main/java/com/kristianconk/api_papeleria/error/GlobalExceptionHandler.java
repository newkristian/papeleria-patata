package com.kristianconk.api_papeleria.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<ErrorResponse> manejarAccesoDenegado(final AccesoDenegadoException e) {
        log.error("[POS/GlobalExceptionHandler] - MANEJAR_ACCESO_DENEGADO: errorMessage: {}", e.getMessage());
        final ErrorResponse error = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "Acceso denegado",
                e.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(org.springframework.security.authorization.AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> manejarAutorizacionDenegada(
            final org.springframework.security.authorization.AuthorizationDeniedException e) {
        log.error("[POS/GlobalExceptionHandler] - MANEJAR_AUTORIZACION_DENEGADA: errorMessage: {}", e.getMessage());
        final ErrorResponse error = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "Acceso denegado",
                "No tiene permisos suficientes para realizar esta operación",
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<ErrorResponse> manejarAutenticacionFallida(
            final org.springframework.security.core.AuthenticationException e) {
        // Mensaje genérico deliberado: nunca se revela si el usuario existe, está
        // inactivo o la contraseña es incorrecta.
        log.warn("[POS/GlobalExceptionHandler] - MANEJAR_AUTENTICACION_FALLIDA: {}", e.getClass().getSimpleName());
        final ErrorResponse error = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Autenticación fallida",
                "Usuario o contraseña incorrectos, o la cuenta no está activa",
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> manejarResourceNotFound(final ResourceNotFoundException e) {
        log.error("[POS/GlobalExceptionHandler] - MANEJAR_RESOURCE_NOT_FOUND: errorMessage: {}", e.getMessage());
        final ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Recurso no encontrado",
                e.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> manejarValidacion(final org.springframework.web.bind.MethodArgumentNotValidException e) {
        final String mensaje = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(java.util.stream.Collectors.joining(", "));
        log.error("[POS/GlobalExceptionHandler] - MANEJAR_VALIDACION: errorMessage: {}", mensaje);
        final ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Error de validación",
                mensaje,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> manejarJsonInvalido(
            final org.springframework.http.converter.HttpMessageNotReadableException e) {
        final Throwable causaRaiz = causaMasProfunda(e);
        final String mensaje = causaRaiz instanceof IllegalArgumentException
                ? causaRaiz.getMessage()
                : "El cuerpo de la solicitud no tiene un formato válido";
        log.error("[POS/GlobalExceptionHandler] - MANEJAR_JSON_INVALIDO: errorMessage: {}", mensaje);
        final ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Error de validación",
                mensaje,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    private Throwable causaMasProfunda(final Throwable e) {
        Throwable actual = e;
        while (actual.getCause() != null && actual.getCause() != actual) {
            actual = actual.getCause();
        }
        return actual;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> manejarIllegalArgument(final IllegalArgumentException e) {
        log.error("[POS/GlobalExceptionHandler] - MANEJAR_ILLEGAL_ARGUMENT: errorMessage: {}", e.getMessage());
        final ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Argumento inválido",
                e.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> manejarIllegalState(final IllegalStateException e) {
        log.error("[POS/GlobalExceptionHandler] - MANEJAR_ILLEGAL_STATE: errorMessage: {}", e.getMessage());
        final ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Estado inválido",
                e.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> manejarExcepcionGenerica(final Exception e) {
        log.error("[POS/GlobalExceptionHandler] - EXCEPCION_GENERICA: errorMessage: {}", e.getMessage(), e);
        final ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error interno del servidor",
                "Ha ocurrido un error inesperado en el servidor.",
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

