package com.kristianconk.api_papeleria.auth.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        boolean requiereCambioPassword
) {}
