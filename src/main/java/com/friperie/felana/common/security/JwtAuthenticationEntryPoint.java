package com.friperie.felana.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Sans cette classe, Spring Security renvoie par défaut un 403 pour toute
 * requête non authentifiée (token absent, invalide ou expiré) - ce qui est
 * incorrect sémantiquement, et surtout casse le mécanisme de refresh côté
 * frontend, qui n'écoute que les 401.
 *
 * 401 = "je ne sais pas qui vous êtes" (token absent/invalide/expiré)
 * 403 = "je sais qui vous êtes, mais vous n'avez pas le droit" (mauvais rôle)
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("error", "Authentification requise ou token expiré.");

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}