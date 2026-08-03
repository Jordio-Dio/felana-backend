package com.friperie.felana.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Encapsule l'envoi d'emails. Isolé dans son propre service pour que, plus
 * tard, on puisse remplacer JavaMailSender par un provider transactionnel
 * (SendGrid, Mailjet...) sans toucher au reste du code métier.
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendOtpEmail(String to, String code, String subject) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText("Votre code de vérification Felana est : " + code
                + "\n\nCe code expire dans 10 minutes. Si vous n'êtes pas à l'origine "
                + "de cette demande, ignorez cet email.");
        mailSender.send(message);
    }
}