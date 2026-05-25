package com.kristianconk.api_papeleria.tienda;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/tiendas")
@RequiredArgsConstructor
@Tag(name = "Tiendas", description = "API para la gestion de tiendas")
public class TiendaController {

    private final TiendaService tiendaService;

    @GetMapping
    @Operation(summary = "Obtener todas las tiendas")
    public ResponseEntity<List<TiendaResponseDTO>> obtenerTodas() {
        final List<TiendaResponseDTO> tiendas = tiendaService.obtenerTodas();
        return ResponseEntity.ok(tiendas);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de tienda por ID")
    public ResponseEntity<TiendaResponseDTO> obtenerPorId(final @PathVariable Long id) {
        final TiendaResponseDTO tienda = tiendaService.obtenerPorId(id);
        return ResponseEntity.ok(tienda);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Crear una nueva tienda (Solo ADMINISTRADOR)")
    public ResponseEntity<TiendaResponseDTO> crear(final @Valid @RequestBody TiendaRequestDTO request) {
        final TiendaResponseDTO tienda = tiendaService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(tienda);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Actualizar una tienda existente (Solo ADMINISTRADOR)")
    public ResponseEntity<TiendaResponseDTO> actualizar(
            final @PathVariable Long id,
            final @Valid @RequestBody TiendaRequestDTO request) {
        final TiendaResponseDTO tienda = tiendaService.actualizar(id, request);
        return ResponseEntity.ok(tienda);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Eliminar una tienda (Solo ADMINISTRADOR)")
    public ResponseEntity<Void> eliminar(final @PathVariable Long id) {
        tiendaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
