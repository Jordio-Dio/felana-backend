package com.friperie.felana.shop.service;

import com.friperie.felana.auth.security.JwtService;
import com.friperie.felana.orders.domain.Client;
import com.friperie.felana.orders.repository.ClientRepository;
import com.friperie.felana.shop.dto.request.ClientLoginRequest;
import com.friperie.felana.shop.dto.request.ClientRegisterRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientAuthServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private JwtService jwtService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void login_shouldAcceptPhoneWithPlus261PrefixAndSpaces() {
        String rawPassword = "Azerty123!";
        Client client = Client.builder()
                .id(1L)
                .nom("Rakoto")
                .prenom("Jean")
                .telephone("0341234567")
                .password(passwordEncoder.encode(rawPassword))
                .compteActif(true)
                .build();

        when(clientRepository.findByTelephone("0341234567")).thenReturn(Optional.of(client));
        when(jwtService.generateClientToken(any(Client.class))).thenReturn("jwt-client-token");

        ClientAuthService service = new ClientAuthService(clientRepository, passwordEncoder, jwtService);
        var result = service.login(new ClientLoginRequest("+261 34 123 45 67", rawPassword));

        assertNotNull(result);
        assertEquals("jwt-client-token", result.token());
        assertEquals(1L, result.id());
    }

    @Test
    void register_shouldNormalizePhoneAndEncodePasswordBeforeSaving() {
        ClientRegisterRequest request = new ClientRegisterRequest(
                "Rakoto",
                "Jean",
                "",
                "+261 34 123 45 67",
                "Azerty123!"
        );

        when(clientRepository.findByTelephone("0341234567")).thenReturn(Optional.empty());
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateClientToken(any(Client.class))).thenReturn("jwt-client-token");

        ClientAuthService service = new ClientAuthService(clientRepository, passwordEncoder, jwtService);
        var result = service.register(request);

        ArgumentCaptor<Client> clientCaptor = ArgumentCaptor.forClass(Client.class);
        verify(clientRepository).save(clientCaptor.capture());

        Client savedClient = clientCaptor.getValue();

        assertNotNull(result);
        assertEquals("jwt-client-token", result.token());
        assertEquals("0341234567", savedClient.getTelephone());
        assertNotEquals("Azerty123!", savedClient.getPassword());
        assertTrue(passwordEncoder.matches("Azerty123!", savedClient.getPassword()));
    }
}
