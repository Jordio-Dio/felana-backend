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
     * Numéro de téléphone obligatoire et unique servant d'identifiant principal.
     */
    @Column(nullable = false, unique = true)
    private String telephone;

    @Column(length = 500)
    private String adresse;

    @Column(nullable = false, updatable = false)
    private Instant dateCreation;

    /**
     * Mot de passe encodé (BCrypt), NULL si le client commande en invité
     * ou si la fiche est créée par un vendeur sans compte.
     */
    @Column
    private String password;

    /**
     * true si ce client possède un compte actif avec mot de passe,
     * false s'il s'agit d'une commande invité ou fiche boutique.
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

    /** Identifiant de connexion unique : Numéro de téléphone. */
    @Override
    public String getUsername() {
        return telephone;
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