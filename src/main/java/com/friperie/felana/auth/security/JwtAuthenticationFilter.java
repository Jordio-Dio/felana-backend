package com.friperie.felana.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtre exécuté UNE FOIS par requête (OncePerRequestFilter), placé avant le
 * filtre standard de Spring Security qui gère l'authentification par
 * user/password. Son rôle :
 *
 * 1. Lire l'en-tête "Authorization: Bearer <token>".
 * 2. Extraire le username du JWT.
 * 3. Charger le UserDetails correspondant en base.
 * 4. Si le token est valide, "peupler" le SecurityContext avec un objet
 * Authentication : c'est CE qui fait que @PreAuthorize("hasRole('GERANT')")
 * peut ensuite fonctionner plus loin dans la requête.
 *
 * Si aucun header n'est présent, ou s'il est invalide, on laisse simplement
 * passer la requête sans authentifier l'utilisateur : c'est la suite de la
 * chaîne de filtres (SecurityConfig) qui décidera si la route nécessite une
 * authentification ou non.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Pas de header, ou pas au format attendu -> on laisse passer sans
        // authentifier.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7); // on retire "Bearer "
        final String username;

        try {
            username = jwtService.extractUsername(jwt);
        } catch (Exception e) {
            // Token malformé, signature invalide, expiré... -> on ne bloque pas ici,
            // on laisse simplement l'utilisateur non authentifié. C'est SecurityConfig
            // qui renverra un 401/403 si la route l'exige.
            filterChain.doFilter(request, response);
            return;
        }

        // On n'authentifie que si : un username a été extrait ET personne n'est
        // déjà authentifié dans le contexte courant (évite un travail redondant).
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // pas de credentials nécessaires, on est déjà authentifié via le token
                        userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        return path.startsWith("/v3/api-docs/")
                || path.startsWith("/swagger-ui/"); 
    }
}
