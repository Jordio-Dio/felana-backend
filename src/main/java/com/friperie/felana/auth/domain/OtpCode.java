package com.friperie.felana.auth.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Code OTP à usage unique, pour la vérification d'email ou la réinitialisation
 * de mot de passe. Stocké haché-non (c'est un code court à courte durée de
 * vie, le risque est acceptable), mais toujours à usage unique et expirant.
 */
@Entity
@Table(name = "otp_codes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nullable : un OTP peut être lié à un User (staff) OU juste à un email
     * autonome (client), jamais les deux en même temps.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    /** Email cible pour les OTP clients, sans relation vers une entité User. */
    @Column
    private String emailCible;

    @Column(nullable = false, length = 6)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OtpPurpose purpose;

    @Column(nullable = false)
    private Instant expiryDate;

    @Column(nullable = false)
    @Builder.Default
    private boolean used = false;

    public boolean isExpired() {
        return expiryDate.isBefore(Instant.now());
    }
}