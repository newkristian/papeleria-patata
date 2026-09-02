package com.kristianconk.api_papeleria.autorizacion;

import com.kristianconk.api_papeleria.error.AccesoDenegadoException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limita intentos fallidos repetidos de reautenticación de gerente/administrador por
 * vendedor solicitante (T6), para dificultar que un vendedor use este endpoint como
 * oráculo de fuerza bruta contra credenciales ajenas.
 *
 * Implementación en memoria: no persiste entre reinicios ni se comparte entre
 * instancias del backend. Suficiente para un despliegue de instancia única (el
 * `docker-compose` de este proyecto); si el backend llegara a escalar horizontalmente
 * debe migrarse a un almacén compartido (p. ej. Redis) antes de confiar en este
 * límite.
 */
@Component
class LimitadorIntentosAutorizacion {

    private static final int MAX_INTENTOS_FALLIDOS = 5;
    private static final Duration VENTANA = Duration.ofMinutes(15);

    private final Map<Long, Estado> intentosPorVendedor = new ConcurrentHashMap<>();

    void verificarDisponible(final Long vendedorId) {
        final Estado estado = intentosPorVendedor.get(vendedorId);
        if (estado != null && estado.bloqueadoHasta != null && Instant.now().isBefore(estado.bloqueadoHasta)) {
            throw new AccesoDenegadoException(
                    "Demasiados intentos fallidos de autorización. Intente de nuevo más tarde.");
        }
    }

    void registrarFallo(final Long vendedorId) {
        intentosPorVendedor.compute(vendedorId, (id, actual) -> {
            final Estado estado = actual == null ? new Estado() : actual;
            estado.registrarFallo();
            return estado;
        });
    }

    void registrarExito(final Long vendedorId) {
        intentosPorVendedor.remove(vendedorId);
    }

    private static final class Estado {
        private int fallos = 0;
        private Instant primerFalloVentana;
        private Instant bloqueadoHasta;

        void registrarFallo() {
            final Instant ahora = Instant.now();
            if (primerFalloVentana == null || Duration.between(primerFalloVentana, ahora).compareTo(VENTANA) > 0) {
                primerFalloVentana = ahora;
                fallos = 0;
            }
            fallos++;
            if (fallos >= MAX_INTENTOS_FALLIDOS) {
                bloqueadoHasta = ahora.plus(VENTANA);
            }
        }
    }
}
