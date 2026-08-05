package com.friperie.felana.auth.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuration centrale de Spring Security.
 *
 * @EnableMethodSecurity active la sécurité au niveau des méthodes, ce qui est
 *                       INDISPENSABLE pour
 *                       que @PreAuthorize("hasRole('GERANT')") fonctionne sur
 *                       le endpoint /api/auth/register-vendeur.
 *
 *                       Points clés :
 *                       - SessionCreationPolicy.STATELESS : on n'utilise PAS de
 *                       session HTTP côté
 *                       serveur. Chaque requête doit s'authentifier elle-même
 *                       via le JWT.
 *                       C'est cohérent avec une architecture JWT.
 *                       - Le filtre JwtAuthenticationFilter est inséré AVANT
 *                       UsernamePasswordAuthenticationFilter dans la chaîne.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
        .requestMatchers(
                "/v3/api-docs",
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html"
        ).permitAll()
        .requestMatchers(
                "/auth/login",
                "/auth/refresh-token",
                "/auth/verify-email",
                "/auth/resend-verification",
                "/auth/forgot-password",
                "/auth/reset-password"
        ).permitAll()
        .requestMatchers(HttpMethod.GET, "/categories/**").permitAll()
        .anyRequest().authenticated()
)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Le "pont" entre Spring Security et notre UserDetailsService/PasswordEncoder :
     * il sait comment charger un utilisateur et vérifier son mot de passe.
     * Utilisé par l'AuthenticationManager lors du login.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Composant utilisé dans AuthenticationService pour déclencher manuellement
     * l'authentification (vérification username/password) lors du login.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt : hachage à sens unique + salage automatique, standard de fait pour
        // les mots de passe.
        return new BCryptPasswordEncoder();
    }

    /**
     * Configuration CORS minimale : à adapter avec le(s) domaine(s) réel(s)
     * de votre frontend en production (éviter "*" avec des credentials).
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:8080"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


}
