package com.kristianconk.api_papeleria.inventario;

import com.kristianconk.api_papeleria.enums.RolUsuario;
import com.kristianconk.api_papeleria.enums.TipoMovimiento;
import com.kristianconk.api_papeleria.usuario.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/inventario")
@RequiredArgsConstructor
@Tag(name = "Inventario", description = "API para registrar movimientos y consultar el historial de inventario")
public class InventarioController {

    private final InventarioService inventarioService;

    @PostMapping("/entradas")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'INVENTARISTA')")
    @Operation(summary = "Registrar entrada manual de inventario (Solo ADMINISTRADOR, GERENTE e INVENTARISTA)")
    public ResponseEntity<InventarioMovimientoResponseDTO> registrarEntrada(
            final @Valid @RequestBody InventarioMovimientoRequestDTO request,
            final @AuthenticationPrincipal Usuario usuario) {
        log.info("[POS/InventarioController] - REGISTRAR_ENTRADA: iniciando registro para productoId: {}, userId: {}", 
                request.productoId(), usuario.getId());
        final InventarioMovimiento movimiento = inventarioService.registrarEntrada(request, usuario);
        final InventarioMovimientoResponseDTO response = InventarioMovimientoMapper.toDto(movimiento, false);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/salidas")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'INVENTARISTA')")
    @Operation(summary = "Registrar salida manual de inventario (Solo ADMINISTRADOR, GERENTE e INVENTARISTA)")
    public ResponseEntity<InventarioMovimientoResponseDTO> registrarSalida(
            final @Valid @RequestBody InventarioMovimientoRequestDTO request,
            final @AuthenticationPrincipal Usuario usuario) {
        log.info("[POS/InventarioController] - REGISTRAR_SALIDA: iniciando registro para productoId: {}, userId: {}", 
                request.productoId(), usuario.getId());
        final InventarioMovimiento movimiento = inventarioService.registrarSalida(request, usuario);
        final InventarioMovimientoResponseDTO response = InventarioMovimientoMapper.toDto(movimiento, false);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/movimientos")
    @Operation(summary = "Consultar historial de movimientos con filtros y paginación (Público autenticado)")
    public ResponseEntity<Page<InventarioMovimientoResponseDTO>> obtenerMovimientos(
            final @RequestParam(required = false) Long productoId,
            final @RequestParam(required = false) TipoMovimiento tipo,
            final @RequestParam(required = false) Long usuarioId,
            final @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            final @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            final @PageableDefault(size = 20) Pageable pageable,
            final @AuthenticationPrincipal Usuario usuario) {
        log.info("[POS/InventarioController] - OBTENER_MOVIMIENTOS: consultando filtros, userId: {}", usuario.getId());

        final Page<InventarioMovimiento> movimientos = inventarioService.obtenerMovimientos(
                productoId, tipo, usuarioId, fechaInicio, fechaFin, pageable);

        final boolean ocultarCosto = usuario.getRol() == RolUsuario.VENDEDOR;
        final Page<InventarioMovimientoResponseDTO> responsePage = movimientos.map(
                mov -> InventarioMovimientoMapper.toDto(mov, ocultarCosto));

        return ResponseEntity.ok(responsePage);
    }
}
