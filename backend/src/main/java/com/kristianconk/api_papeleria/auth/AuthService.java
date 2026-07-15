package com.kristianconk.api_papeleria.auth;

import com.kristianconk.api_papeleria.auth.dto.AuthResponse;
import com.kristianconk.api_papeleria.auth.dto.LoginRequest;
import com.kristianconk.api_papeleria.auth.dto.RegisterRequest;
import com.kristianconk.api_papeleria.security.jwt.JwtService;
import com.kristianconk.api_papeleria.usuario.Usuario;
import com.kristianconk.api_papeleria.usuario.UsuarioCreateRequestDTO;
import com.kristianconk.api_papeleria.usuario.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioService usuarioService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    public AuthResponse register(final RegisterRequest request) {
        log.info("[POS/AuthService] - REGISTER: Registrando nuevo usuario con email: {}", request.email());

        final UsuarioCreateRequestDTO createRequest = new UsuarioCreateRequestDTO(
                request.nombre(),
                null,
                request.email(),
                request.password(),
                request.role(),
                null
        );
        usuarioService.crear(createRequest);

        final String accessToken = "";
        final String refreshToken = "";

        log.info("[POS/AuthService] - REGISTER: Usuario registrado con éxito: {}", request.email());
        return new AuthResponse(accessToken, refreshToken, true);
    }

    public AuthResponse login(final LoginRequest request) {
        log.info("[POS/AuthService] - LOGIN: Autenticando usuario con username: {}", request.username());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()));

        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());

        final String accessToken = jwtService.generateToken(userDetails);
        final String refreshToken = jwtService.generateRefreshToken(userDetails);

        boolean requiereCambioPassword = false;
        if (userDetails instanceof Usuario) {
            requiereCambioPassword = ((Usuario) userDetails).isRequiereCambioPassword();
        }

        log.info("[POS/AuthService] - LOGIN: Usuario autenticado exitosamente: {}", request.username());
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
