package com.friperie.felana.auth.service;

import com.friperie.felana.auth.domain.Role;
import com.friperie.felana.auth.domain.User;
import com.friperie.felana.auth.dto.request.ChangePasswordRequest;
import com.friperie.felana.auth.dto.request.UpdateProfileRequest;
import com.friperie.felana.auth.exception.SelfActionForbiddenException;
import com.friperie.felana.auth.repository.UserRepository;
import com.friperie.felana.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gestion des comptes utilisateurs (consultation, activation/désactivation).
 * La CREATION d'un vendeur reste dans AuthenticationService.registerVendeur()
 * - on ne duplique pas cette logique ici, ce service se concentre sur la
 * lecture et la gestion du cycle de vie d'un compte déjà créé.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Page<User> findVendeurs(Pageable pageable) {
        return userRepository.findByRole(Role.VENDEUR, pageable);
    }

    public User findEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable, id=" + id));
    }

    /**
     * Active ou désactive un compte. Un GERANT ne peut jamais se désactiver
     * lui-même (garde-fou pour éviter de se retrouver bloqué hors de
     * l'application sans autre compte GERANT pour le réactiver).
     */
    @Transactional
    public User updateStatut(Long id, boolean enabled, User utilisateurConnecte) {
        User cible = findEntityById(id);

        if (cible.getId().equals(utilisateurConnecte.getId()) && !enabled) {
            throw new SelfActionForbiddenException(
                    "Vous ne pouvez pas désactiver votre propre compte.");
        }

        cible.setEnabled(enabled);
        return userRepository.save(cible);
    }

    @Transactional
public User updateProfile(Long userId, UpdateProfileRequest request) {
    User user = findEntityById(userId);

    boolean emailChange = !user.getEmail().equalsIgnoreCase(request.email());
    if (emailChange && userRepository.existsByEmail(request.email())) {
        throw new DataIntegrityViolationException("Cet email est déjà utilisé.");
    }

    user.setName(request.name());
    user.setEmail(request.email());
    if (emailChange) {
        user.setEmailVerified(false); // à revérifier, comme à l'inscription
    }
    return userRepository.save(user);
}

@Transactional
public void changePassword(Long userId, ChangePasswordRequest request) {
    User user = findEntityById(userId);
    if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
        throw new BadCredentialsException("Mot de passe actuel incorrect.");
    }
    user.setPassword(passwordEncoder.encode(request.newPassword()));
    userRepository.save(user);
}
}   