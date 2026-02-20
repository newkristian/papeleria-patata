package com.kristianconk.api_papeleria.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterRequest {
    private String nombre;
    private String email;
    private String password;
    private String role; // Ej. "ADMIN" o "CASHIER"

    // Getters y Setters
}
