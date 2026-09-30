package com.friperie.felana.shop.service;

import com.friperie.felana.auth.dto.request.ChangePasswordRequest;
import com.friperie.felana.auth.security.JwtService;
import com.friperie.felana.common.exception.ResourceNotFoundException;
import com.friperie.felana.orders.domain.Client;
import com.friperie.felana.orders.repository.ClientRepository;
import com.friperie.felana.shop.dto.request.ClientLoginRequest;
import com.friperie.felana.shop.dto.request.ClientRegisterRequest;
import com.friperie.felana.shop.dto.request.ClientUpdateProfileRequest;
import com.friperie.felana.shop.dto.response.ClientAuthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClientAuthService {

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private static String normalizeTelephone(String telephone) {
        if (telephone == null) {
            return null;
        }

        String digits = telephone.trim().replaceAll("\\s+", "");
        digits = digits.replace("+", "");
        digits = digits.replaceAll("[^0-9]", "");

        if (digits.isEmpty()) {
            return "";
        }

        if (digits.startsWith("261")) {
            digits = "0" + digits.substring(3);
        }

        if (!digits.startsWith("0") && digits.length() == 9) {
            digits = "0" + digits;
        }

        return digits;
    }

    @Transactional
    public ClientAuthResponse register(ClientRegisterRequest request) {
        if (request.telephone() == null || request.telephone().isBlank()) {
            throw new IllegalArgumentException("Le numéro de téléphone est obligatoire.");
        }

        String normalizedTelephone = normalizeTelephone(request.telephone());

        Client client = clientRepository.findByTelephone(normalizedTelephone)
                .orElseGet(() -> Client.builder()
                        .nom(request.nom())
                        .prenom(request.prenom())
                        .telephone(normalizedTelephone)
                        .build());

        if (client.isCompteActif() && client.getPassword() != null) {
            throw new IllegalArgumentException("Ce numéro de téléphone est déjà associé à un compte.");
        }

        client.setNom(request.nom());
        client.setPrenom(request.prenom());
        client.setTelephone(normalizedTelephone);
        client.setPassword(passwordEncoder.encode(request.password()));
        client.setCompteActif(true);

        client = clientRepository.save(client);

        String token = jwtService.generateClientToken(client);
        return new ClientAuthResponse(client.getId(), token, client.getNom());
    }

    @Transactional(readOnly = true)
    public ClientAuthResponse login(ClientLoginRequest request) {
        String normalizedTelephone = normalizeTelephone(request.identifiant());

        Client client = clientRepository.findByTelephone(normalizedTelephone)
                .orElseThrow(() -> new BadCredentialsException("Téléphone ou mot de passe incorrect."));

        if (!client.isCompteActif() || client.getPassword() == null || !passwordEncoder.matches(request.password(), client.getPassword())) {
            throw new BadCredentialsException("Téléphone ou mot de passe incorrect.");
        }

        String token = jwtService.generateClientToken(client);
        return new ClientAuthResponse(client.getId(), token, client.getNom());
    }

    @Transactional
    public ClientAuthResponse updateProfile(Long clientId, ClientUpdateProfileRequest request) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable."));

        if (request.telephone() == null || request.telephone().isBlank()) {
            throw new IllegalArgumentException("Le numéro de téléphone est obligatoire.");
        }

        String normalizedTelephone = normalizeTelephone(request.telephone());
        boolean telephoneChange = !client.getTelephone().equals(normalizedTelephone);
        if (telephoneChange) {
            clientRepository.findByTelephone(normalizedTelephone).ifPresent(existing -> {
                if (!existing.getId().equals(clientId)) {
                    throw new IllegalArgumentException("Ce numéro de téléphone est déjà utilisé.");
                }
            });
        }

        client.setNom(request.nom());
        client.setPrenom(request.prenom());
        client.setTelephone(normalizedTelephone);
        client.setAdresse(request.adresse());

        client = clientRepository.save(client);
        String token = jwtService.generateClientToken(client);
        return new ClientAuthResponse(client.getId(), token, client.getNom());
    }

    @Transactional
    public ClientAuthResponse changePassword(Long clientId, ChangePasswordRequest request) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable."));

        if (!passwordEncoder.matches(request.currentPassword(), client.getPassword())) {
            throw new BadCredentialsException("Mot de passe actuel incorrect.");
        }

        client.setPassword(passwordEncoder.encode(request.newPassword()));
        client = clientRepository.save(client);

        String token = jwtService.generateClientToken(client);
        return new ClientAuthResponse(client.getId(), token, client.getNom());
    }
}