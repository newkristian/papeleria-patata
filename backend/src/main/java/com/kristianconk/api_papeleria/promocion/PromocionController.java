package com.kristianconk.api_papeleria.promocion;

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
@RequestMapping("/api/v1/promociones")
@RequiredArgsConstructor
@Tag(name = "Promociones", description = "API para la gestión de promociones automáticas de producto")
public class PromocionController {

    private final PromocionService promocionService;

    @GetMapping
    @Operation(summary = "Obtener todas las promociones")
    public ResponseEntity<List<PromocionResponseDTO>> obtenerTodas() {
        final List<PromocionResponseDTO> promociones = promocionService.obtenerTodas();
        return ResponseEntity.ok(promociones);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de promoción por ID")
    public ResponseEntity<PromocionResponseDTO> obtenerPorId(final @PathVariable Long id) {
        final PromocionResponseDTO promocion = promocionService.obtenerPorId(id);
        return ResponseEntity.ok(promocion);
    }

    @GetMapping("/producto/{productoId}")
    @Operation(summary = "Listar promociones aplicables a un producto")
    public ResponseEntity<List<PromocionResponseDTO>> obtenerPorProducto(final @PathVariable Long productoId) {
        final List<PromocionResponseDTO> promociones = promocionService.obtenerPorProducto(productoId);
        return ResponseEntity.ok(promociones);
    }

    @GetMapping("/categoria/{categoriaId}")
    @Operation(summary = "Listar promociones aplicables a una categoría")
    public ResponseEntity<List<PromocionResponseDTO>> obtenerPorCategoria(final @PathVariable Long categoriaId) {
        final List<PromocionResponseDTO> promociones = promocionService.obtenerPorCategoria(categoriaId);
        return ResponseEntity.ok(promociones);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Crear una nueva promoción (Solo ADMINISTRADOR)")
    public ResponseEntity<PromocionResponseDTO> crear(final @Valid @RequestBody PromocionRequestDTO request) {
        final PromocionResponseDTO promocion = promocionService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(promocion);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Actualizar una promoción existente (Solo ADMINISTRADOR)")
    public ResponseEntity<PromocionResponseDTO> actualizar(
            final @PathVariable Long id,
            final @Valid @RequestBody PromocionRequestDTO request) {
        final PromocionResponseDTO promocion = promocionService.actualizar(id, request);
        return ResponseEntity.ok(promocion);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Eliminar una promoción (Solo ADMINISTRADOR)")
    public ResponseEntity<Void> eliminar(final @PathVariable Long id) {
        promocionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
