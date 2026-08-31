package com.friperie.felana.orders.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    private String prenom;

    /**
     * Optionnel : un client final n'a pas forcément d'email (vente en boutique
     * physique).
     */
    private String email;

    private String telephone;

    @Column(length = 500)
    private String adresse;

    @Column(nullable = false, updatable = false)
    private Instant dateCreation;

    /**
     * Mot de passe encodé (BCrypt), NULL si le client n'a pas encore de compte
     * (cas des fiches créées manuellement par un vendeur en boutique).
     */
    @Column
    private String password;

    /**
     * true uniquement si le compte a été vérifié par OTP email.
     * Toujours true si inscription par téléphone seul (pas de vérification).
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean emailVerifie = false;

    /**
     * true si ce client a un vrai compte de connexion (email ou téléphone +
     * mot de passe), false s'il s'agit d'une fiche créée manuellement par un
     * vendeur sans intention de connexion.
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean compteActif = false;

    @PrePersist
    protected void onCreate() {
        this.dateCreation = Instant.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_CLIENT"));
    }

    @Override
    public String getPassword() {
        return password;
    }

    /** Identifiant de connexion : email si présent, sinon téléphone. */
    @Override
    public String getUsername() {
        return email != null ? email : telephone;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() {
        return compteActif;
    }
}