package com.friperie.felana.shop.service;

import com.friperie.felana.auth.service.OtpService;
import com.friperie.felana.common.exception.ResourceNotFoundException;
import com.friperie.felana.auth.domain.OtpPurpose;
import com.friperie.felana.auth.dto.request.ChangePasswordRequest;
import com.friperie.felana.orders.domain.Client;
import com.friperie.felana.orders.dto.response.ClientResponse;
import com.friperie.felana.orders.repository.ClientRepository;
import com.friperie.felana.shop.dto.response.ClientAuthResponse;
import com.friperie.felana.shop.dto.request.ClientLoginRequest;
import com.friperie.felana.shop.dto.request.ClientRegisterRequest;
import com.friperie.felana.shop.dto.request.ClientUpdateProfileRequest;
import com.friperie.felana.auth.security.JwtService;
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
    private final OtpService otpService; // réutilise le service OTP déjà existant

    @Transactional
    public ClientAuthResponse register(ClientRegisterRequest request) {
        if (!request.hasEmail() && !request.hasTelephone()) {
            throw new IllegalArgumentException("Un email ou un numéro de téléphone est obligatoire.");
        }
        if (request.hasEmail() && clientRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Cet email est déjà utilisé.");
        }
        if (request.hasTelephone() && clientRepository.findByTelephone(request.telephone()).isPresent()) {
            throw new IllegalArgumentException("Ce numéro de téléphone est déjà utilisé.");
        }

        // Par téléphone seul : pas d'OTP, compte actif immédiatement.
        boolean emailVerifieParDefaut = !request.hasEmail();

        Client client = Client.builder()
                .nom(request.nom())
                .prenom(request.prenom())
                .email(request.hasEmail() ? request.email() : null)
                .telephone(request.hasTelephone() ? request.telephone() : null)
                .password(passwordEncoder.encode(request.password()))
                .compteActif(true)
                .emailVerifie(emailVerifieParDefaut)
                .build();

        client = clientRepository.save(client);

        if (request.hasEmail()) {
            otpService.generateAndSend(client.getEmail(), OtpPurpose.EMAIL_VERIFICATION);
        }

        String token = jwtService.generateClientToken(client);
        return new ClientAuthResponse(client.getId(), token, client.getNom(), client.isEmailVerifie());
    }

    @Transactional(readOnly = true)
    public ClientAuthResponse login(ClientLoginRequest request) {
        Client client = clientRepository.findByEmail(request.identifiant())
                .or(() -> clientRepository.findByTelephone(request.identifiant()))
                .orElseThrow(() -> new BadCredentialsException("Identifiant ou mot de passe incorrect."));

        if (!client.isCompteActif() || !passwordEncoder.matches(request.password(), client.getPassword())) {
            throw new BadCredentialsException("Identifiant ou mot de passe incorrect.");
        }

        String token = jwtService.generateClientToken(client);
        return new ClientAuthResponse(client.getId(), token, client.getNom(), client.isEmailVerifie());
    }

    @Transactional
    public ClientAuthResponse updateProfile(Long clientId, ClientUpdateProfileRequest request) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable."));

        if (!request.hasEmail() && !request.hasTelephone()) {
            throw new IllegalArgumentException("Un email ou un numéro de téléphone est obligatoire.");
        }

        boolean emailChange = request.hasEmail()
                && (client.getEmail() == null || !client.getEmail().equalsIgnoreCase(request.email()));
        if (emailChange) {
            clientRepository.findByEmail(request.email()).ifPresent(existing -> {
                if (!existing.getId().equals(clientId)) {
                    throw new IllegalArgumentException("Cet email est déjà utilisé.");
                }
            });
        }

        boolean telephoneChange = request.hasTelephone()
                && (client.getTelephone() == null || !client.getTelephone().equals(request.telephone()));
        if (telephoneChange) {
            clientRepository.findByTelephone(request.telephone()).ifPresent(existing -> {
                if (!existing.getId().equals(clientId)) {
                    throw new IllegalArgumentException("Ce numéro de téléphone est déjà utilisé.");
                }
            });
        }

        client.setNom(request.nom());
        client.setPrenom(request.prenom());
        client.setEmail(request.hasEmail() ? request.email() : null);
        client.setTelephone(request.hasTelephone() ? request.telephone() : null);
        client.setAdresse(request.adresse());

        if (emailChange) {
            client.setEmailVerifie(false);
        }

        client = clientRepository.save(client);
        String token = jwtService.generateClientToken(client);
        return new ClientAuthResponse(client.getId(), token, client.getNom(), client.isEmailVerifie());
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
        return new ClientAuthResponse(client.getId(), token, client.getNom(), client.isEmailVerifie());
    }
}