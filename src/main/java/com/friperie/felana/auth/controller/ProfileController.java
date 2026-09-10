package com.friperie.felana.auth.controller;

import com.friperie.felana.auth.domain.User;
import com.friperie.felana.auth.dto.request.ChangePasswordRequest;
import com.friperie.felana.auth.dto.request.UpdateProfileRequest;
import com.friperie.felana.auth.dto.response.UserResponse;
import com.friperie.felana.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** Profil du STAFF connecté (GERANT ou VENDEUR) - accessible aux deux rôles,
 *  contrairement à UserController qui est réservé au GERANT. */
@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('GERANT','VENDEUR')")
@Tag(name = "Mon profil", description = "Gestion du profil du compte staff connecté")
public class ProfileController {

    private final UserService userService;

    @Operation(summary = "Voir mon profil")
    @GetMapping
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @Operation(summary = "Modifier mon nom/email")
    @PatchMapping
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(UserResponse.from(userService.updateProfile(user.getId(), request)));
    }

    @Operation(summary = "Changer mon mot de passe")
    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(user.getId(), request);
        return ResponseEntity.noContent().build();
    }
}