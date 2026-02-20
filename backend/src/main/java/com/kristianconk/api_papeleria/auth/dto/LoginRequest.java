package com.kristianconk.api_papeleria.auth.dto;

public record LoginRequest (
    String email,
    String password
){}
