package com.kristianconk.api_papeleria.error;

import java.time.LocalDateTime;

public record ErrorResponse(
        int status,
        String error,
        String mensaje,
        LocalDateTime timestamp
) {}

