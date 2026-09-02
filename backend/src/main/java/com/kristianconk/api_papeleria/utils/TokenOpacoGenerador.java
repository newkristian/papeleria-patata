package com.kristianconk.api_papeleria.utils;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Genera referencias opacas de un solo uso (p. ej. autorizaciones de descuento manual,
 * T6) y su hash. Solo el hash se persiste; el valor en claro se entrega una única vez
 * al cliente y no puede reconstruirse a partir de lo guardado en base de datos.
 */
@Component
public class TokenOpacoGenerador {

    private static final int LONGITUD_BYTES = 32; // 256 bits de entropía
    private static final SecureRandom ALEATORIO = new SecureRandom();

    public String generar() {
        final byte[] valores = new byte[LONGITUD_BYTES];
        ALEATORIO.nextBytes(valores);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(valores);
    }

    public String hash(final String tokenPlano) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hash = digest.digest(tokenPlano.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().withoutPadding().encodeToString(hash);
        } catch (final NoSuchAlgorithmException e) {
            // SHA-256 es un algoritmo estándar de la JVM; esta rama es inalcanzable en
            // la práctica.
            throw new IllegalStateException("Algoritmo de hash no disponible", e);
        }
    }
}
