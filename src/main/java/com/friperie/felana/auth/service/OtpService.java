package com.friperie.felana.auth.service;

import com.friperie.felana.auth.domain.OtpCode;
import com.friperie.felana.auth.domain.OtpPurpose;
import com.friperie.felana.auth.domain.User;
import com.friperie.felana.auth.exception.TokenRefreshException;
import com.friperie.felana.auth.repository.OtpCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpCodeRepository otpCodeRepository;
    private final EmailService emailService;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Value("${app.otp.expiration-minutes:10}")
    private long expirationMinutes;

    /** Génère un code à 6 chiffres, le persiste, puis l'envoie par email. */
    @Transactional
    public void generateAndSend(User user, OtpPurpose purpose) {
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));

        OtpCode otp = OtpCode.builder()
                .user(user)
                .code(code)
                .purpose(purpose)
                .expiryDate(Instant.now().plusSeconds(expirationMinutes * 60))
                .used(false)
                .build();
        otpCodeRepository.save(otp);

        String subject = purpose == OtpPurpose.EMAIL_VERIFICATION
                ? "Vérification de votre email Felana"
                : "Réinitialisation de votre mot de passe Felana";
        emailService.sendOtpEmail(user.getEmail(), code, subject);
    }

    /**
     * Vérifie le code fourni ; le marque comme utilisé si valide. Lève une
     * exception sinon.
     */
    @Transactional
    public OtpCode verify(User user, OtpPurpose purpose, String code) {
        OtpCode otp = otpCodeRepository
                .findTopByUserAndPurposeAndUsedFalseOrderByIdDesc(user, purpose)
                .orElseThrow(() -> new TokenRefreshException("Aucun code actif pour cet utilisateur."));

        if (otp.isExpired()) {
            throw new TokenRefreshException("Ce code a expiré, veuillez en redemander un.");
        }
        if (!otp.getCode().equals(code)) {
            throw new TokenRefreshException("Code incorrect.");
        }

        otp.setUsed(true);
        otpCodeRepository.save(otp);
        return otp;
    }

    @Transactional
    public void generateAndSend(String email, OtpPurpose purpose) {
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));

        OtpCode otp = OtpCode.builder()
                .emailCible(email)
                .code(code)
                .purpose(purpose)
                .expiryDate(Instant.now().plusSeconds(expirationMinutes * 60))
                .used(false)
                .build();
        otpCodeRepository.save(otp);

        emailService.sendOtpEmail(email, code, "Vérification de votre email Felana");
    }

    @Transactional
    public void verify(String email, OtpPurpose purpose, String code) {
        OtpCode otp = otpCodeRepository
                .findTopByEmailCibleAndPurposeAndUsedFalseOrderByIdDesc(email, purpose)
                .orElseThrow(() -> new TokenRefreshException("Aucun code actif pour cet email."));

        if (otp.isExpired()) {
            throw new TokenRefreshException("Ce code a expiré, veuillez en redemander un.");
        }
        if (!otp.getCode().equals(code)) {
            throw new TokenRefreshException("Code incorrect.");
        }

        otp.setUsed(true);
        otpCodeRepository.save(otp);
    }
}