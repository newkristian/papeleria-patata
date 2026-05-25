package com.kristianconk.api_papeleria.config;

import com.kristianconk.api_papeleria.security.jwt.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, UserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Habilitar CORS
                .csrf(csrf -> csrf.disable()) // Comúnmente deshabilitado cuando el frontend (ej. Angular) y backend
                                              // están separados
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v*/auth/**").permitAll()
                        // Jerarquía de roles para el POS
                        // Admin: acceso total a productos y usuarios
                        .requestMatchers("/api/v*/admin/**").hasRole("ADMINISTRADOR")
                        // Ventas: cajeros y admin pueden crear ventas
                        .requestMatchers("/api/v*/ventas/**").hasAnyRole("ADMINISTRADOR", "VENDEDOR")
                        // Productos y catálogos: autenticado
                        .requestMatchers("/api/v*/productos/**").authenticated()
                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // .authenticationProvider(authenticationProvider())
                .authenticationManager(authenticationManager())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(authProvider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Permite el puerto de tu frontend
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        // Cabeceras permitidas (muy importante permitir Authorization para tu JWT)
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
        // Permite el envío de credenciales/cookies si fuera necesario
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Aplica a todos los endpoints
        return source;
    }
}
