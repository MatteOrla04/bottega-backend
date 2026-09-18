package com.bottega.bottega_web.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // 1. Creiamo il motore per criptare le password
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. Diciamo a Spring Security di non bloccare ancora le nostre pagine
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()) // Disabilitiamo protezioni extra per lo sviluppo
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // Per ora lasciamo tutto "aperto"
            );
        return http.build();
    }
}