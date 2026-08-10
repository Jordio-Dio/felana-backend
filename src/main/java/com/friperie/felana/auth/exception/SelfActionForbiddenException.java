package com.friperie.felana.auth.exception;

/**
 * Levée quand un GERANT tente une action dangereuse sur son propre compte
 * (ex: se désactiver lui-même), ce qui le bloquerait hors de l'application
 * sans recours (à part réintervenir directement en base).
 */
public class SelfActionForbiddenException extends RuntimeException {
    public SelfActionForbiddenException(String message) {
        super(message);
    }
}