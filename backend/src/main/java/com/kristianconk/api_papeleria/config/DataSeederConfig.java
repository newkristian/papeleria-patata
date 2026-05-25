package com.kristianconk.api_papeleria.config;

import com.kristianconk.api_papeleria.enums.RolUsuario;
import com.kristianconk.api_papeleria.usuario.Usuario;
import com.kristianconk.api_papeleria.usuario.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
public class DataSeederConfig {

    @Bean
    public CommandLineRunner initDatabase(UsuarioRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Solo insertamos si la tabla está vacía para evitar duplicados si cambias a una BD persistente luego
            if (userRepository.count() == 0) {

                // Usuario Administrador para el Punto de Venta
                Usuario admin = new Usuario();
                admin.setNombre("Administrador del Sistema");
                admin.setEmail("admin@pos.com");
                admin.setUsername("admin@pos.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRol(RolUsuario.ADMINISTRADOR); // O "ROLE_ADMIN" dependiendo de cómo lo manejes en tu entidad

                // Usuario Cajero
                Usuario cashier = new Usuario();
                cashier.setNombre("Cajero Principal");
                cashier.setEmail("caja@pos.com");
                cashier.setUsername("caja@pos.com");
                cashier.setPassword(passwordEncoder.encode("caja123"));
                cashier.setRol(RolUsuario.VENDEDOR);

                userRepository.saveAll(List.of(admin, cashier));

                System.out.println("✅ Base de datos PostgreSQL inicializada con usuarios de prueba para el POS.");
            } else {
                System.out.println("⚡ La base de datos ya contiene usuarios. Se omite la inicialización.");
            }
        };
    }
}