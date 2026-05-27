package com.kristianconk.api_papeleria.auth;

import com.kristianconk.api_papeleria.auth.dto.AuthResponse;
import com.kristianconk.api_papeleria.auth.dto.LoginRequest;
import com.kristianconk.api_papeleria.auth.dto.RegisterRequest;
import com.kristianconk.api_papeleria.enums.RolUsuario;
import com.kristianconk.api_papeleria.security.jwt.JwtService;
import com.kristianconk.api_papeleria.usuario.Usuario;
import com.kristianconk.api_papeleria.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    public AuthResponse register(final RegisterRequest request) {
        log.info("[POS/AuthService] - REGISTER: Registrando nuevo usuario con email: {}", request.getEmail());

        final RolUsuario rol;
        try {
            rol = RolUsuario.valueOf(request.getRole().toUpperCase());
        } catch (final IllegalArgumentException e) {
            log.error("[POS/AuthService] - REGISTER: Rol inválido recibido: {}", request.getRole());
            throw new IllegalArgumentException("El rol '" + request.getRole() + "' no es un rol válido del sistema.");
        }

        final Usuario user = new Usuario();
        user.setNombre(request.getNombre());
        user.setUsername(request.getEmail());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRol(rol);
        user.setRequiereCambioPassword(true);

        userRepository.save(user);

        final String accessToken = jwtService.generateToken(user);
        final String refreshToken = jwtService.generateRefreshToken(user);

        log.info("[POS/AuthService] - REGISTER: Usuario registrado con éxito: {}", request.getEmail());
        return new AuthResponse(accessToken, refreshToken, true);
    }

    public AuthResponse login(final LoginRequest request) {
        log.info("[POS/AuthService] - LOGIN: Autenticando usuario con email: {}", request.email());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());

        final String accessToken = jwtService.generateToken(userDetails);
        final String refreshToken = jwtService.generateRefreshToken(userDetails);

        boolean requiereCambioPassword = false;
        if (userDetails instanceof Usuario) {
            requiereCambioPassword = ((Usuario) userDetails).isRequiereCambioPassword();
        }

        log.info("[POS/AuthService] - LOGIN: Usuario autenticado exitosamente: {}", request.email());
        return new AuthResponse(accessToken, refreshToken, requiereCambioPassword);
    }

    public AuthResponse refreshToken(final String refreshToken) {
        log.info("[POS/AuthService] - REFRESH_TOKEN: Refrescando token");
        final String userEmail = jwtService.extractUsername(refreshToken);

        if (userEmail != null) {
            final UserDetails user = userDetailsService.loadUserByUsername(userEmail);

            if (jwtService.isTokenValid(refreshToken, user)) {
                final String newAccessToken = jwtService.generateToken(user);

                boolean requiereCambioPassword = false;
                if (user instanceof Usuario) {
                    requiereCambioPassword = ((Usuario) user).isRequiereCambioPassword();
                }

                log.info("[POS/AuthService] - REFRESH_TOKEN: Token refrescado con éxito para usuario: {}", userEmail);
                return new AuthResponse(newAccessToken, refreshToken, requiereCambioPassword);
            }
        }
        log.error("[POS/AuthService] - REFRESH_TOKEN: Error al refrescar token, token inválido o expirado");
        throw new IllegalArgumentException("Refresh token inválido");
    }
}
