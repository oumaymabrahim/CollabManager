package com.proxym.collabmanager.controllers;

import com.proxym.collabmanager.dto.AuthResponse;
import com.proxym.collabmanager.dto.LoginRequest;
import com.proxym.collabmanager.dto.RefreshTokenRequest;
import com.proxym.collabmanager.dto.RegisterRequest;
import com.proxym.collabmanager.entities.Utilisateur;
import com.proxym.collabmanager.services.AuthService;
import com.proxym.collabmanager.services.UtilisateurService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final UtilisateurService utilisateurService;

    /**
     * Inscription d'un nouvel utilisateur
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Connexion d'un utilisateur
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Rafraîchissement du token
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            // Validation supplémentaire côté contrôleur
            if (request == null || request.getToken() == null || request.getToken().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "error", "Token manquant",
                                "message", "Le token de rafraîchissement est obligatoire"
                        ));
            }

            AuthResponse response = authService.refreshToken(request);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "error", e.getMessage(),
                            "details", "Vérifiez que le token est valide et non vide"
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Erreur serveur lors du rafraîchissement",
                            "message", e.getMessage()
                    ));
        }
    }

    /**
     * Validation du token
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            boolean isValid = authService.validateToken(token);
            return ResponseEntity.ok(Map.of("valid", isValid));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("valid", false));
        }
    }

    /**
     * Récupérer le profil de l'utilisateur connecté
     */
    /**
     * Récupérer le profil de l'utilisateur connecté - AVEC DEBUG
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        try {
            // Récupérer l'utilisateur connecté via Spring Security Context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // Vérifier si l'authentification existe et est valide
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                                "error", "Non authentifié",
                                "message", "Token invalide ou manquant"
                        ));
            }

            // Récupérer l'email de l'utilisateur
            String email = authentication.getName();

            // Vérifier si l'email est valide (pas "anonymousUser")
            if (email == null || email.equals("anonymousUser")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                                "error", "Utilisateur anonyme",
                                "message", "Aucun utilisateur authentifié trouvé"
                        ));
            }

            // Chercher l'utilisateur par email
            Optional<Utilisateur> utilisateurOpt = utilisateurService.getByEmail(email);

            if (utilisateurOpt.isPresent()) {
                Utilisateur utilisateur = utilisateurOpt.get();
                return ResponseEntity.ok(Map.of(
                        "id", utilisateur.getId(),
                        "nom", utilisateur.getNom(),
                        "email", utilisateur.getEmail(),
                        "role", utilisateur.getRole().toString(),
                        "message", "Profil récupéré avec succès"
                ));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "error", "Utilisateur non trouvé",
                                "message", "Aucun utilisateur trouvé avec l'email: " + email
                        ));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Erreur serveur",
                            "message", "Erreur lors de la récupération du profil: " + e.getMessage()
                    ));
        }
    }

    /**
     * Déconnexion (côté client, invalider le token)
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("message", "Déconnexion réussie"));
    }

    /**
     * Vérifier si un email existe déjà
     */
    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        try {
            boolean exists = authService.emailExists(email); // Correction du nom de méthode
            return ResponseEntity.ok(Map.of("exists", exists));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("exists", false));
        }
    }
}