package com.kristianconk.api_papeleria.usuario;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "API para la gestión de usuarios y control de acceso")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping("/perfil")
    @Operation(summary = "Obtener perfil del usuario autenticado actual")
    public ResponseEntity<UsuarioPerfilDTO> obtenerPerfil(final Principal principal) {
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        final UsuarioPerfilDTO perfil = usuarioService.obtenerPerfilPorUsername(principal.getName());
        return ResponseEntity.ok(perfil);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Listar todos los usuarios (Solo ADMINISTRADOR)")
    public ResponseEntity<List<UsuarioResponseDTO>> obtenerTodos() {
        final List<UsuarioResponseDTO> usuarios = usuarioService.obtenerTodos();
        return ResponseEntity.ok(usuarios);
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Obtener un usuario por ID (Solo ADMINISTRADOR)")
    public ResponseEntity<UsuarioResponseDTO> obtenerPorId(final @PathVariable Long id) {
        final UsuarioResponseDTO usuario = usuarioService.obtenerPorId(id);
        return ResponseEntity.ok(usuario);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Crear un nuevo usuario (Solo ADMINISTRADOR)")
    public ResponseEntity<UsuarioResponseDTO> crear(final @Valid @RequestBody UsuarioCreateRequestDTO request) {
        final UsuarioResponseDTO usuario = usuarioService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Actualizar datos de un usuario (Solo ADMINISTRADOR)")
    public ResponseEntity<UsuarioResponseDTO> actualizar(
            final @PathVariable Long id,
            final @Valid @RequestBody UsuarioUpdateRequestDTO request) {
        final UsuarioResponseDTO usuario = usuarioService.actualizar(id, request);
        return ResponseEntity.ok(usuario);
    }

    @PatchMapping("/{id}/activar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Activar un usuario (Solo ADMINISTRADOR)")
    public ResponseEntity<Void> activar(final @PathVariable Long id) {
        usuarioService.activar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Desactivar un usuario (Solo ADMINISTRADOR)")
    public ResponseEntity<Void> desactivar(final @PathVariable Long id) {
        usuarioService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/cambiar-password")
    @Operation(summary = "Cambiar contraseña del usuario autenticado")
    public ResponseEntity<Void> cambiarPassword(
            final Principal principal,
            final @Valid @RequestBody CambioPasswordRequestDTO request) {
        usuarioService.cambiarPassword(principal.getName(), request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Restablecer contraseña de un usuario (Solo ADMINISTRADOR)")
    public ResponseEntity<Void> resetPassword(
            final @PathVariable Long id,
            final @Valid @RequestBody ResetPasswordRequestDTO request) {
        usuarioService.resetPassword(id, request.password());
        return ResponseEntity.noContent().build();
    }
}
