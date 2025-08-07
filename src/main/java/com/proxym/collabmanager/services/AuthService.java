package com.proxym.collabmanager.services;

import com.proxym.collabmanager.dto.AuthResponse;
import com.proxym.collabmanager.dto.LoginRequest;
import com.proxym.collabmanager.dto.RefreshTokenRequest;
import com.proxym.collabmanager.dto.RegisterRequest;
import com.proxym.collabmanager.entities.Utilisateur;
import com.proxym.collabmanager.repositories.UtilisateurRepository;
import com.proxym.collabmanager.services.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder; // Injection directe du PasswordEncoder
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Inscription d'un nouvel utilisateur
     */
    public AuthResponse register(RegisterRequest request) {
        // Vérifier si l'email existe déjà
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà");
        }

        // Créer le nouvel utilisateur
        var utilisateur = new Utilisateur();
        utilisateur.setNom(request.getNom());
        utilisateur.setEmail(request.getEmail());
        utilisateur.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
        utilisateur.setRole(request.getRole());

        // Sauvegarder l'utilisateur
        utilisateur = utilisateurRepository.save(utilisateur);

        // Générer le token JWT
        var jwtToken = jwtService.generateToken(utilisateur);

        return AuthResponse.builder()
                .token(jwtToken)
                .userId(utilisateur.getId())
                .nom(utilisateur.getNom())
                .email(utilisateur.getEmail())
                .role(utilisateur.getRole())
                .message("Inscription réussie")
                .build();
    }

    /**
     * Connexion d'un utilisateur existant
     */
    public AuthResponse login(LoginRequest request) {
        try {
            // Authentifier l'utilisateur
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getMotDePasse()
                    )
            );

            // Récupérer l'utilisateur
            var utilisateur = utilisateurRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

            // Générer le token JWT
            var jwtToken = jwtService.generateToken(utilisateur);

            return AuthResponse.builder()
                    .token(jwtToken)
                    .userId(utilisateur.getId())
                    .nom(utilisateur.getNom())
                    .email(utilisateur.getEmail())
                    .role(utilisateur.getRole())
                    .message("Connexion réussie")
                    .build();

        } catch (BadCredentialsException e) {
            throw new RuntimeException("Email ou mot de passe incorrect");
        }
    }

    /**
     * Rafraîchissement du token
     */
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        try {
            String token = request.getToken();

            // VALIDATION : Vérifier que le token n'est pas null ou vide
            if (token == null || token.trim().isEmpty()) {
                throw new RuntimeException("Token de rafraîchissement manquant ou vide");
            }

            // Nettoyer le token (enlever "Bearer " s'il est présent)
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            // Vérifier encore une fois après nettoyage
            if (token.trim().isEmpty()) {
                throw new RuntimeException("Token de rafraîchissement invalide après nettoyage");
            }

            // Extraire l'email du token (même s'il est expiré)
            String userEmail = jwtService.extractUsernameFromExpiredToken(token);

            if (userEmail != null && !userEmail.trim().isEmpty()) {
                // Récupérer l'utilisateur
                var utilisateur = utilisateurRepository.findByEmail(userEmail)
                        .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé pour l'email: " + userEmail));

                // Générer un nouveau token
                var newToken = jwtService.generateToken(utilisateur);

                return AuthResponse.builder()
                        .token(newToken)
                        .userId(utilisateur.getId())
                        .nom(utilisateur.getNom())
                        .email(utilisateur.getEmail())
                        .role(utilisateur.getRole())
                        .message("Token rafraîchi avec succès")
                        .build();
            }

            throw new RuntimeException("Impossible d'extraire l'utilisateur du token");

        } catch (Exception e) {
            // Log détaillé pour debug
            System.err.println("Erreur refresh token: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors du rafraîchissement du token: " + e.getMessage());
        }
    }

    /**
     * Validation du token
     */
    public boolean validateToken(String token) {
        try {
            String userEmail = jwtService.extractUsername(token);
            if (userEmail != null) {
                var utilisateur = utilisateurRepository.findByEmail(userEmail)
                        .orElse(null);
                return utilisateur != null && jwtService.isTokenValid(token, utilisateur);
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Vérifier si un email existe
     */
    public boolean emailExists(String email) {
        return utilisateurRepository.existsByEmail(email);
    }

    /**
     * Récupérer les informations de l'utilisateur à partir du token
     */
    public Utilisateur getUserFromToken(String token) {
        try {
            String userEmail = jwtService.extractUsername(token);
            return utilisateurRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));
        } catch (Exception e) {
            throw new RuntimeException("Token invalide");
        }
    }
}