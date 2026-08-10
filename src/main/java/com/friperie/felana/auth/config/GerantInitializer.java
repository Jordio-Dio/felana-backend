package com.friperie.felana.auth.config;


import com.friperie.felana.auth.domain.Role;
import com.friperie.felana.auth.domain.User;
import com.friperie.felana.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
 
/**
 * Crée le tout premier compte GERANT au démarrage de l'application, s'il
 * n'en existe aucun en base.
 *
 * Pourquoi pas un endpoint HTTP "/register-gerant" ? Parce que n'importe qui
 * pourrait y accéder au premier lancement (avant qu'un GERANT existe), ce qui
 * serait une faille de sécurité critique : le premier arrivé sur le serveur
 * pourrait s'auto-attribuer les droits GERANT.
 *
 * Ici, seule la personne qui a accès aux variables d'environnement du
 * serveur (vous, l'administrateur système) peut définir ces identifiants.
 * Une fois le premier GERANT créé, ce CommandLineRunner ne fait plus rien
 * aux démarrages suivants (il vérifie l'existence avant de créer).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GerantInitializer implements CommandLineRunner {
 
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
 
    @Value("${app.bootstrap.gerant.name:}")
    private String defaultName;
 
    @Value("${app.bootstrap.gerant.password:}")
    private String defaultPassword;

    @Value("${app.bootstrap.gerant.email:gerant@admin.com}")
    private String defaultEmail;
 
    @Override
    public void run(String... args) {
        // Si un GERANT existe déjà quelque part, on ne fait rien.
        if (userRepository.existsByEmail(defaultEmail)) {
            log.info("Compte GERANT '{}' déjà présent, aucune action au démarrage.", defaultName);
            return;
        }
 
        if (defaultEmail.isBlank() || defaultPassword.isBlank()) {
            log.warn("Aucune propriété/variable d'environnement configurée pour le gérant. / "
                    + "APP_BOOTSTRAP_GERANT_PASSWORD définie : aucun compte GERANT "
                    + "n'a été créé automatiquement. Définissez-les puis redémarrez "
                    + "l'application pour créer le premier compte administrateur.");
            return;
        }
 
        User gerant = User.builder()
                .name(defaultName)
                .email(defaultEmail)
                .password(passwordEncoder.encode(defaultPassword))
                .role(Role.GERANT)
                .enabled(true)
                .build();
 
        userRepository.save(gerant);
        log.info("Premier compte GERANT '{}' créé avec succès.", defaultEmail);
    }
}
 
