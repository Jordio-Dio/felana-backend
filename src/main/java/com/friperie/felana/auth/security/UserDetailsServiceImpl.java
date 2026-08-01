package com.friperie.felana.auth.security;

import com.friperie.felana.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implémentation de l'interface UserDetailsService attendue par Spring
 * Security. Son unique rôle : étant donné un username, retrouver
 * l'utilisateur correspondant en base. Comme notre entité User implémente
 * déjà UserDetails, on peut la retourner directement.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Aucun utilisateur trouvé avec le username : " + username));
    }
}
