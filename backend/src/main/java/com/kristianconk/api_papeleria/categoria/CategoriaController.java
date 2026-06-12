package com.kristianconk.api_papeleria.categoria;

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
@RequestMapping("/api/v1/categorias")
@RequiredArgsConstructor
@Tag(name = "Categorías", description = "API para la gestión de categorías de productos")
public class CategoriaController {

    private final CategoriaService categoriaService;

    @GetMapping
    @Operation(summary = "Obtener todas las categorías")
    public ResponseEntity<List<CategoriaResponseDTO>> obtenerTodas() {
        final List<CategoriaResponseDTO> categorias = categoriaService.obtenerTodas();
        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de categoría por ID")
    public ResponseEntity<CategoriaResponseDTO> obtenerPorId(final @PathVariable Long id) {
        final CategoriaResponseDTO categoria = categoriaService.obtenerPorId(id);
        return ResponseEntity.ok(categoria);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'INVENTARISTA')")
    @Operation(summary = "Crear una nueva categoría (Solo ADMINISTRADOR e INVENTARISTA)")
    public ResponseEntity<CategoriaResponseDTO> crear(final @Valid @RequestBody CategoriaRequestDTO request) {
        final CategoriaResponseDTO categoria = categoriaService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoria);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'INVENTARISTA')")
    @Operation(summary = "Actualizar una categoría existente (Solo ADMINISTRADOR e INVENTARISTA)")
    public ResponseEntity<CategoriaResponseDTO> actualizar(
            final @PathVariable Long id,
            final @Valid @RequestBody CategoriaRequestDTO request) {
        final CategoriaResponseDTO categoria = categoriaService.actualizar(id, request);
        return ResponseEntity.ok(categoria);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'INVENTARISTA')")
    @Operation(summary = "Eliminar una categoría (Solo ADMINISTRADOR e INVENTARISTA)")
    public ResponseEntity<Void> eliminar(final @PathVariable Long id) {
        categoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
