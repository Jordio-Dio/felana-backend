package com.friperie.felana.orders.service;

import com.friperie.felana.common.exception.ResourceNotFoundException;
import com.friperie.felana.orders.domain.Client;
import com.friperie.felana.orders.dto.request.ClientRequest;
import com.friperie.felana.orders.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    public Page<Client> findAll(Pageable pageable) {
        return clientRepository.findAll(pageable);
    }

    public Client findEntityById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable, id=" + id));
    }

    @Transactional
    public Client create(ClientRequest request) {
        Client client = Client.builder()
                .nom(request.nom())
                .prenom(request.prenom())
                .email(request.email())
                .telephone(request.telephone())
                .adresse(request.adresse())
                .build();
        return clientRepository.save(client);
    }

    @Transactional
    public Client update(Long id, ClientRequest request) {
        Client client = findEntityById(id);
        client.setNom(request.nom());
        client.setPrenom(request.prenom());
        client.setEmail(request.email());
        client.setTelephone(request.telephone());
        client.setAdresse(request.adresse());
        return clientRepository.save(client);
    }

    @Transactional
    public void delete(Long id) {
        clientRepository.delete(findEntityById(id));
    }
}