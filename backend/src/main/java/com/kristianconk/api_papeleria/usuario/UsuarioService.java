package com.kristianconk.api_papeleria.usuario;

import com.kristianconk.api_papeleria.error.ResourceNotFoundException;
import com.kristianconk.api_papeleria.tienda.Tienda;
import com.kristianconk.api_papeleria.tienda.TiendaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final TiendaRepository tiendaRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> obtenerTodos() {
        log.info("[POS/UsuarioService] - OBTENER_TODAS: buscando todos los usuarios");
        final List<Usuario> usuarios = usuarioRepository.findAll();
        return usuarios.stream()
                .map(UsuarioMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtenerPorId(final Long id) {
        log.info("[POS/UsuarioService] - OBTENER_POR_ID: buscando usuario con ID: {}", id);
        if (id == null) {
            log.error("[POS/UsuarioService] - OBTENER_POR_ID: ID de usuario es nulo");
            throw new IllegalArgumentException("El ID del usuario no puede ser nulo");
        }
        final Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("[POS/UsuarioService] - OBTENER_POR_ID: usuario con ID: {} no encontrado", id);
                    return new ResourceNotFoundException("El usuario con ID " + id + " no existe");
                });
        return UsuarioMapper.toDto(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioPerfilDTO obtenerPerfilPorUsername(final String username) {
        log.info("[POS/UsuarioService] - OBTENER_PERFIL: buscando perfil de usuario: {}", username);
        if (username == null || username.isBlank()) {
            log.error("[POS/UsuarioService] - OBTENER_PERFIL: username es nulo o vacío");
            throw new IllegalArgumentException("El username no puede ser nulo o vacío");
        }
        final Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("[POS/UsuarioService] - OBTENER_PERFIL: usuario '{}' no encontrado", username);
                    return new ResourceNotFoundException("Usuario no encontrado");
                });
        return UsuarioMapper.toPerfilDto(usuario);
    }


    public UsuarioResponseDTO crear(final UsuarioCreateRequestDTO request) {
        log.info("[POS/UsuarioService] - CREAR: creando nuevo usuario con email: {}", request.email());
        if (usuarioRepository.existsByEmail(request.email())) {
            log.error("[POS/UsuarioService] - CREAR: email {} ya está registrado", request.email());
            throw new IllegalArgumentException("El email ya está registrado");
        }

        final Tienda tienda = obtenerTiendaSiIdPresente(request.tiendaId());

        final String encodedPassword = passwordEncoder.encode(request.password());
        final Usuario usuario = UsuarioMapper.toEntity(request, tienda, encodedPassword);
        final Usuario guardado = usuarioRepository.save(usuario);

        log.info("[POS/UsuarioService] - CREAR: usuario creado con ID: {}", guardado.getId());
        return UsuarioMapper.toDto(guardado);
    }

    public UsuarioResponseDTO actualizar(final Long id, final UsuarioUpdateRequestDTO request) {
        log.info("[POS/UsuarioService] - ACTUALIZAR: actualizando usuario con ID: {}", id);
        if (id == null) {
            log.error("[POS/UsuarioService] - ACTUALIZAR: ID de usuario es nulo");
            throw new IllegalArgumentException("El ID del usuario no puede ser nulo");
        }

        final Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("[POS/UsuarioService] - ACTUALIZAR: usuario con ID: {} no encontrado", id);
                    return new ResourceNotFoundException("El usuario con ID " + id + " no existe");
                });

        final boolean emailCambio = !usuarioExistente.getEmail().equals(request.email());
        if (emailCambio && usuarioRepository.existsByEmail(request.email())) {
            log.error("[POS/UsuarioService] - ACTUALIZAR: email {} ya está registrado", request.email());
            throw new IllegalArgumentException("El email ya está registrado");
        }

        final Tienda tienda = obtenerTiendaSiIdPresente(request.tiendaId());

        UsuarioMapper.updateEntity(usuarioExistente, request, tienda);
        final Usuario guardado = usuarioRepository.save(usuarioExistente);

        log.info("[POS/UsuarioService] - ACTUALIZAR: usuario con ID: {} actualizado", id);
        return UsuarioMapper.toDto(guardado);
    }

    public void activar(final Long id) {
        log.info("[POS/UsuarioService] - ACTIVAR: activando usuario con ID: {}", id);
        if (id == null) {
            log.error("[POS/UsuarioService] - ACTIVAR: ID de usuario es nulo");
            throw new IllegalArgumentException("El ID del usuario no puede ser nulo");
        }
        final Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("[POS/UsuarioService] - ACTIVAR: usuario con ID: {} no encontrado", id);
                    return new ResourceNotFoundException("El usuario con ID " + id + " no existe");
                });
        usuario.setActivo(true);
        usuarioRepository.save(usuario);
        log.info("[POS/UsuarioService] - ACTIVAR: usuario con ID: {} activado", id);
    }

    public void desactivar(final Long id) {
        log.info("[POS/UsuarioService] - DESACTIVAR: desactivando usuario con ID: {}", id);
        if (id == null) {
            log.error("[POS/UsuarioService] - DESACTIVAR: ID de usuario es nulo");
            throw new IllegalArgumentException("El ID del usuario no puede ser nulo");
        }
        final Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("[POS/UsuarioService] - DESACTIVAR: usuario con ID: {} no encontrado", id);
                    return new ResourceNotFoundException("El usuario con ID " + id + " no existe");
                });
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
        log.info("[POS/UsuarioService] - DESACTIVAR: usuario con ID: {} desactivado", id);
    }

    public void cambiarPassword(final String username, final CambioPasswordRequestDTO request) {
        log.info("[POS/UsuarioService] - CAMBIAR_PASSWORD: cambiando contraseña para usuario: {}", username);
        if (username == null) {
            log.error("[POS/UsuarioService] - CAMBIAR_PASSWORD: username es nulo");
            throw new IllegalArgumentException("El username no puede ser nulo");
        }
        final Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("[POS/UsuarioService] - CAMBIAR_PASSWORD: usuario {} no encontrado", username);
                    return new ResourceNotFoundException("El usuario no existe");
                });

        final boolean matches = passwordEncoder.matches(request.oldPassword(), usuario.getPassword());
        if (!matches) {
            log.error("[POS/UsuarioService] - CAMBIAR_PASSWORD: clave anterior incorrecta para usuario: {}", username);
            throw new IllegalArgumentException("La contraseña anterior es incorrecta");
        }

        usuario.setPassword(passwordEncoder.encode(request.newPassword()));
        usuario.setRequiereCambioPassword(false);
        usuarioRepository.save(usuario);
        log.info("[POS/UsuarioService] - CAMBIAR_PASSWORD: clave cambiada con éxito para usuario: {}", username);
    }

    public void resetPassword(final Long id, final String newPassword) {
        log.info("[POS/UsuarioService] - RESET_PASSWORD: restableciendo contraseña para usuario ID: {}", id);
        if (id == null) {
            log.error("[POS/UsuarioService] - RESET_PASSWORD: ID de usuario es nulo");
            throw new IllegalArgumentException("El ID del usuario no puede ser nulo");
        }
        if (newPassword == null || newPassword.isBlank()) {
            log.error("[POS/UsuarioService] - RESET_PASSWORD: nueva contraseña está vacía");
            throw new IllegalArgumentException("La nueva contraseña no puede estar vacía");
        }

        final Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("[POS/UsuarioService] - RESET_PASSWORD: usuario con ID: {} no encontrado", id);
                    return new ResourceNotFoundException("El usuario con ID " + id + " no existe");
                });

        usuario.setPassword(passwordEncoder.encode(newPassword));
        usuario.setRequiereCambioPassword(true);
        usuarioRepository.save(usuario);
        log.info("[POS/UsuarioService] - RESET_PASSWORD: clave restablecida con éxito para usuario ID: {}", id);
    }

    private Tienda obtenerTiendaSiIdPresente(final Long tiendaId) {
        if (tiendaId == null) {
            return null;
        }
        return tiendaRepository.findById(tiendaId)
                .orElseThrow(() -> {
                    log.error("[POS/UsuarioService] - BUSCAR_TIENDA: tienda con ID: {} no encontrada", tiendaId);
                    return new ResourceNotFoundException("La tienda con ID " + tiendaId + " no existe");
                });
    }
}
