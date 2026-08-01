package com.friperie.felana.auth.exception;

/**
 * Exception métier levée quand un refresh token est introuvable, expiré ou
 * révoqué. On la distingue des exceptions techniques pour pouvoir la
 * mapper proprement vers un code HTTP 401/403 (voir GlobalExceptionHandler,
 * à ajouter dans le module "common" du monolithe).
 */
public class TokenRefreshException extends RuntimeException {
    public TokenRefreshException(String message) {
        super(message);
    }
}
