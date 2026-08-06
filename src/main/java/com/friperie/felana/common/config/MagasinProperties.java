package com.friperie.felana.common.config;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties
@Getter
@Setter
public class MagasinProperties {
    private String nom = "Felana";
    private String adresse = "";
    private String telephone = "";
    /** Taux de TVA/taxe en décimal (ex: 0.20 pour 20%). 0 par défaut si non applicable. */
    private BigDecimal tauxTaxe = BigDecimal.ZERO;
}