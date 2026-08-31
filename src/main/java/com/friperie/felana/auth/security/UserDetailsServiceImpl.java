package com.friperie.felana.auth.security;

import com.friperie.felana.auth.repository.UserRepository;
import com.friperie.felana.orders.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implémentation de l'interface UserDetailsService attendue par Spring
 * Security. Son unique rôle : étant donné un identifiant, retrouver le
 * compte correspondant en base - qu'il s'agisse d'un membre du staff
 * (User : GERANT/VENDEUR) ou d'un client de la vitrine (Client).
 *
 * Ordre de recherche : User par email d'abord (staff), puis Client par
 * email, puis Client par téléphone (un client peut s'être inscrit avec
 * l'un ou l'autre comme identifiant de connexion).
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;

    @Override
    public UserDetails loadUserByUsername(String identifiant) throws UsernameNotFoundException {
        return userRepository.findByEmail(identifiant)
                .<UserDetails>map(u -> u)
                .or(() -> clientRepository.findByEmail(identifiant).map(c -> c))
                .or(() -> clientRepository.findByTelephone(identifiant).map(c -> c))
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Aucun compte trouvé pour : " + identifiant));
    }
}