package com.friperie.felana.auth.domain;

/**
 * Rôles métier de l'application.
 * - VENDEUR : gère les commandes, consulte le catalogue (sans les coûts/bénéfices).
 * - GERANT  : hérite de tous les droits VENDEUR + accès aux coûts de revient,
 *             bénéfices, prix des articles, et gestion des comptes vendeurs.
 *
 * On préfixe volontairement les valeurs pour qu'elles correspondent à ce que
 * Spring Security attend quand on utilise hasRole("GERANT") : Spring rajoute
 * automatiquement le préfixe "ROLE_" en interne, donc ici on stocke juste
 * "GERANT" / "VENDEUR" et on laisse le framework faire la concordance.
 */
public enum Role {
    VENDEUR,
    GERANT
}
