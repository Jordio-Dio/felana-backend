package com.friperie.felana.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
        @NotBlank(message = "L'email'est obligatoire")
        @Email(message = "L'email doit être valide")
        String email;

        @NotBlank(message = "Le mot de passe est obligatoire")
        String password;

        // Getters et Setters
        public String getEmail() {
                return email;
        }

        public void setEmail(String email) {
                this.email = email;
        }

        public String getPassword() {
                return password;
        }

        public void setPassword(String password) {
                this.password = password;
        }
}
