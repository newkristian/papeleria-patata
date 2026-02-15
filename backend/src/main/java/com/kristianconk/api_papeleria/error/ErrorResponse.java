package com.kristianconk.api_papeleria.error;

import java.time.LocalDateTime;

// ErrorResponse.java
public record ErrorResponse(
        int status,
        String error,
        String mensaje,
        LocalDateTime timestamp
) {}

