package com.kristianconk.api_papeleria.proveedor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/proveedores")
@RequiredArgsConstructor
@Tag(name = "Proveedores", description = "API para la gestión de proveedores")
public class ProveedorController {

    private final ProveedorService proveedorService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'INVENTARISTA')")
    @Operation(summary = "Listar proveedores activos")
    public ResponseEntity<List<ProveedorResponseDTO>> getAllProveedores() {
        return ResponseEntity.ok(proveedorService.getAllProveedores());
    }

    @GetMapping("/buscar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'INVENTARISTA')")
    @Operation(summary = "Buscar proveedores con paginación y filtro de estado")
    public ResponseEntity<Page<ProveedorResponseDTO>> buscar(
            @RequestParam(required = false) final String termino,
            @RequestParam(required = false) final Boolean activo,
            @PageableDefault(size = 20, sort = "nombre", direction = Sort.Direction.ASC) final Pageable pageable) {
        return ResponseEntity.ok(proveedorService.buscar(termino, activo, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'INVENTARISTA')")
    @Operation(summary = "Obtener proveedor por ID")
    public ResponseEntity<ProveedorResponseDTO> getProveedorById(@PathVariable Long id) {
        return ResponseEntity.ok(proveedorService.getProveedorById(id));
    }

    @GetMapping("/{id}/productos/conteo")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'INVENTARISTA')")
    @Operation(summary = "Obtener conteo de productos asociados a un proveedor")
    public ResponseEntity<Long> contarProductos(@PathVariable Long id) {
        return ResponseEntity.ok(proveedorService.contarProductosAsignados(id));
    }


    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    @Operation(summary = "Crear un nuevo proveedor (Solo ADMINISTRADOR y GERENTE)")
    public ResponseEntity<ProveedorResponseDTO> createProveedor(
            @Valid @RequestBody ProveedorRequestDTO request) {
        return new ResponseEntity<>(proveedorService.createProveedor(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
    @Operation(summary = "Actualizar un proveedor (Solo ADMINISTRADOR y GERENTE)")
    public ResponseEntity<ProveedorResponseDTO> updateProveedor(
            @PathVariable Long id,
            @Valid @RequestBody ProveedorRequestDTO request) {
        return ResponseEntity.ok(proveedorService.updateProveedor(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Desactivar un proveedor y reasignar sus productos (Solo ADMINISTRADOR)")
    public ResponseEntity<Void> deleteProveedor(@PathVariable Long id) {
        proveedorService.deleteProveedor(id);
        return ResponseEntity.noContent().build();
    }
}
