package com.friperie.felana.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.friperie.felana.auth.domain.OtpCode;
import com.friperie.felana.auth.domain.OtpPurpose;
import com.friperie.felana.auth.domain.User;

public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {
    Optional<OtpCode> findTopByUserAndPurposeAndUsedFalseOrderByIdDesc(User user, OtpPurpose purpose);   
    Optional<OtpCode> findTopByEmailCibleAndPurposeAndUsedFalseOrderByIdDesc(String emailCible, OtpPurpose purpose); 
}
