package com.kristianconk.api_papeleria.autorizacion;

import com.kristianconk.api_papeleria.usuario.Usuario;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint dedicado de reautenticación para descuentos manuales (T6). Cualquier
 * usuario autenticado con tienda asignada puede invocarlo como vendedor solicitante;
 * la autoridad real de aprobar el descuento la da la reautenticación del
 * gerente/administrador dentro del propio cuerpo del request, no el rol del llamador.
 */
@RestController
@RequestMapping("/api/v1/autorizaciones-descuento")
@RequiredArgsConstructor
public class AutorizacionDescuentoController {

    private final AutorizacionDescuentoService autorizacionDescuentoService;

    @PostMapping
    public ResponseEntity<AutorizacionDescuentoResponseDTO> solicitar(
            @Valid @RequestBody final SolicitudAutorizacionDescuentoDTO request,
            @AuthenticationPrincipal final Usuario vendedor) {
        final AutorizacionDescuentoResponseDTO response = autorizacionDescuentoService.solicitar(request, vendedor);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
