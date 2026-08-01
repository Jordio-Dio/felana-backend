package com.friperie.felana.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service responsable UNIQUEMENT du JWT "access token" (courte durée).
 * Il ne connaît ni la base de données, ni les refresh tokens : il sait juste
 * fabriquer un token signé et le décoder/valider. C'est le principe de
 * responsabilité unique (SRP) : une classe, un rôle.
 *
 * Le JWT est "stateless" : le serveur ne stocke rien pour le vérifier, il lui
 * suffit de vérifier la signature avec la clé secrète et la date d'expiration.
 * C'est pour ça qu'on peut se permettre une durée de vie courte (ex: 15 min) :
 * en cas de vol, la fenêtre d'exploitation est limitée.
 */
@Service
public class JwtService {

    /** Clé secrète encodée en Base64, définie dans application.yml (jwt.secret). */
    @Value("${jwt.secret}")
    private String secretKey;

    /** Durée de vie de l'access token en millisecondes (ex: 900000 = 15 min). */
    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    /** Extrait le "username" (subject) contenu dans le token. */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /** Extrait n'importe quelle information (claim) du token via une fonction. */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /** Génère un access token pour un utilisateur, sans claims additionnels. */
    public String generateAccessToken(UserDetails userDetails) {
        return generateAccessToken(new HashMap<>(), userDetails);
    }

    /**
     * Génère un access token avec des claims additionnels (ex: le rôle),
     * pratique côté frontend pour éviter un appel réseau juste pour savoir
     * si l'utilisateur est GERANT ou VENDEUR.
     */
    public String generateAccessToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** Vérifie que le token appartient bien à cet utilisateur ET qu'il n'est pas expiré. */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /** Décode et vérifie la signature du token ; lève une exception si invalide/altéré. */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
