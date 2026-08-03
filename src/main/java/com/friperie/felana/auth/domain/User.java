package com.friperie.felana.auth.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

/**
 * Entité représentant un compte (VENDEUR ou GERANT).
 *
 * Choix de conception : User implémente directement UserDetails.
 * C'est l'approche la plus simple et la plus lisible pour un projet de cette
 * taille : Spring Security peut utiliser l'entité telle quelle, sans DTO
 * intermédiaire. Si demain le modèle "User" grossit beaucoup (préférences,
 * profil, etc.), on pourra séparer UserDetails dans une classe adaptateur
 * dédiée, mais ce n'est pas nécessaire ici.
 */
@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames =  "email"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    /** On utilise le nom d'utilisateur (ou l'email) comme identifiant de connexion. */
    @Column(nullable = false, unique = true)
    private String email;

    /** Toujours stocké encodé (BCrypt), jamais en clair. */
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean emailVerified = false;



    /// LES FONCTIONS ET LES METHODES DE L'INTERFACE UserDetails SONT EN BAS DE CLASSE, APRÈS LES GETTERS/SETTERS ET LE CONSTRUCTEUR.   
    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    // ---- Méthodes exigées par l'interface UserDetails ----

    /**
     * Retourne les "autorités" (rôles) de l'utilisateur.
     * Spring Security s'attend à un préfixe "ROLE_" pour que hasRole("GERANT")
     * fonctionne : hasRole("GERANT") vérifie en interne la présence de
     * l'autorité "ROLE_GERANT".
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
