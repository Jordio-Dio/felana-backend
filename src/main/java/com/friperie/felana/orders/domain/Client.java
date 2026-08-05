package com.friperie.felana.orders.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    private String prenom;

    /** Optionnel : un client final n'a pas forcément d'email (vente en boutique physique). */
    private String email;

    private String telephone;

    @Column(length = 500)
    private String adresse;

    @Column(nullable = false, updatable = false)
    private Instant dateCreation;

    @PrePersist
    protected void onCreate() {
        this.dateCreation = Instant.now();
    }
}