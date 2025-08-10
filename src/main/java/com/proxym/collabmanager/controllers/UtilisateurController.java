package com.proxym.collabmanager.controllers;

import com.proxym.collabmanager.entities.Utilisateur;
import com.proxym.collabmanager.enums.Role;
import com.proxym.collabmanager.services.UtilisateurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
public class UtilisateurController {

    private final UtilisateurService utilisateurService;
    private final PasswordEncoder passwordEncoder;

    // Méthode utilitaire pour obtenir l'utilisateur connecté
    private Utilisateur getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return utilisateurService.getByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }

    // Méthode utilitaire pour créer une réponse d'erreur d'accès
    private ResponseEntity<?> createAccessDeniedResponse() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of(
                        "error", "Accès refusé",
                        "message", "Vous n'avez pas les permissions nécessaires",
                        "status", 403
                ));
    }

    // ===== INSCRIPTION PUBLIQUE SÉCURISÉE =====

    // Inscription publique - FORCER le rôle MEMBRE_EQUIPE uniquement
    @PostMapping("/inscription")
    public ResponseEntity<?> inscription(@Valid @RequestBody Utilisateur utilisateur) {
        try {
            // Vérifier si l'email existe déjà
            if (utilisateurService.getByEmail(utilisateur.getEmail()).isPresent()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Email déjà utilisé",
                                "message", "Un compte avec cet email existe déjà"));
            }

            // SÉCURITÉ : Forcer le rôle à MEMBRE_EQUIPE pour toute inscription publique
            utilisateur.setRole(Role.MEMBRE_EQUIPE);

            // Validation minimale du mot de passe
            if (utilisateur.getMotDePasse() == null || utilisateur.getMotDePasse().length() < 6) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Mot de passe invalide",
                                "message", "Le mot de passe doit contenir au moins 6 caractères"));
            }

            // Encoder le mot de passe
            utilisateur.setMotDePasse(passwordEncoder.encode(utilisateur.getMotDePasse()));

            Utilisateur savedUtilisateur = utilisateurService.saveUtilisateur(utilisateur);

            // Ne pas retourner le mot de passe dans la réponse
            savedUtilisateur.setMotDePasse(null);

            return ResponseEntity.ok(Map.of(
                    "message", "Inscription réussie",
                    "utilisateur", savedUtilisateur
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de l'inscription",
                            "message", e.getMessage()));
        }
    }

    // ===== ENDPOINTS RÉSERVÉS AUX ADMINS =====

    // Créer un utilisateur avec n'importe quel rôle - ADMIN seulement
    @PostMapping("/admin/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUtilisateur(@RequestBody Utilisateur utilisateur) {
        try {
            // L'admin peut créer un utilisateur avec n'importe quel rôle
            utilisateur.setMotDePasse(passwordEncoder.encode(utilisateur.getMotDePasse()));
            Utilisateur savedUtilisateur = utilisateurService.saveUtilisateur(utilisateur);

            // Ne pas retourner le mot de passe
            savedUtilisateur.setMotDePasse(null);

            return ResponseEntity.ok(savedUtilisateur);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la création de l'utilisateur", "message", e.getMessage()));
        }
    }

    // Modifier le rôle d'un utilisateur - ADMIN seulement
    @PutMapping("/admin/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> changerRole(
            @PathVariable Long id,
            @RequestParam Role nouveauRole) {

        try {
            Optional<Utilisateur> utilisateurOpt = utilisateurService.getUtilisateurById(id);
            if (utilisateurOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Utilisateur utilisateur = utilisateurOpt.get();
            utilisateur.setRole(nouveauRole);
            Utilisateur updatedUtilisateur = utilisateurService.saveUtilisateur(utilisateur);

            // Ne pas retourner le mot de passe
            updatedUtilisateur.setMotDePasse(null);

            return ResponseEntity.ok(updatedUtilisateur);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors du changement de rôle", "message", e.getMessage()));
        }
    }

    // NOUVEAU : ADMIN - Mettre à jour un utilisateur (nom, email, rôle) - ADMIN seulement
    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUtilisateurByAdmin(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {

        try {
            Optional<Utilisateur> utilisateurOpt = utilisateurService.getUtilisateurById(id);
            if (utilisateurOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Utilisateur utilisateur = utilisateurOpt.get();

            // Mettre à jour les champs fournis
            if (updates.containsKey("nom")) {
                utilisateur.setNom((String) updates.get("nom"));
            }
            if (updates.containsKey("email")) {
                utilisateur.setEmail((String) updates.get("email"));
            }
            if (updates.containsKey("role")) {
                try {
                    Role nouveauRole = Role.valueOf((String) updates.get("role"));
                    utilisateur.setRole(nouveauRole);
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Rôle invalide", "message", "Le rôle fourni n'est pas valide"));
                }
            }

            Utilisateur updatedUtilisateur = utilisateurService.saveUtilisateur(utilisateur);

            // Ne pas retourner le mot de passe
            updatedUtilisateur.setMotDePasse(null);

            return ResponseEntity.ok(updatedUtilisateur);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la mise à jour", "message", e.getMessage()));
        }
    }



//    // Obtenir tous les utilisateurs - ADMIN seulement
//    @GetMapping("/all")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<?> getAllUtilisateurs() {
//        try {
//            List<Utilisateur> utilisateurs = utilisateurService.getAllUtilisateurs();
//            // Ne pas retourner les mots de passe
//            utilisateurs.forEach(u -> u.setMotDePasse(null));
//            return ResponseEntity.ok(utilisateurs);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Map.of("error", "Erreur lors de la récupération des utilisateurs", "message", e.getMessage()));
//        }
//    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUtilisateurs() {
        try {
            List<Utilisateur> utilisateurs = utilisateurService.getAllUtilisateurs();

            // SOLUTION 1 : Utiliser HashMap pour mélanger les types
            List<Map<String, Object>> utilisateursSimplifies = utilisateurs.stream()
                    .map(u -> {
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("id", u.getId());
                        userMap.put("nom", u.getNom());
                        userMap.put("email", u.getEmail());
                        userMap.put("role", u.getRole().toString());
                        userMap.put("nombreTaches", u.getTaches() != null ? u.getTaches().size() : 0);
                        userMap.put("nombreProjets", u.getProjets() != null ? u.getProjets().size() : 0);
                        return userMap;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(utilisateursSimplifies);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération des utilisateurs", "message", e.getMessage()));
        }
    }









    // NOUVEAU : ADMIN - Obtenir les détails complets d'un utilisateur - ADMIN seulement
    @GetMapping("/admin/{id}/details")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUtilisateurDetails(@PathVariable Long id) {
        try {
            Optional<Utilisateur> utilisateurOpt = utilisateurService.getUtilisateurById(id);
            if (utilisateurOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Utilisateur utilisateur = utilisateurOpt.get();
            // Ne pas retourner le mot de passe
            utilisateur.setMotDePasse(null);

            return ResponseEntity.ok(utilisateur);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération des détails", "message", e.getMessage()));
        }
    }

    // Obtenir un utilisateur par ID - ADMIN seulement (version simple)
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUtilisateurById(@PathVariable Long id) {
        try {
            Optional<Utilisateur> utilisateur = utilisateurService.getUtilisateurById(id);
            if (utilisateur.isPresent()) {
                utilisateur.get().setMotDePasse(null);
            }
            return ResponseEntity.ok(utilisateur);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération de l'utilisateur", "message", e.getMessage()));
        }
    }

    // Recherche par email - ADMIN seulement
    @GetMapping("/email")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getByEmail(@RequestParam String email) {
        try {
            Optional<Utilisateur> utilisateur = utilisateurService.getByEmail(email);
            if (utilisateur.isPresent()) {
                utilisateur.get().setMotDePasse(null);
            }
            return ResponseEntity.ok(utilisateur);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la recherche par email", "message", e.getMessage()));
        }
    }

    // Recherche par nom - ADMIN seulement
    @GetMapping("/nom")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUtilisateursParNom(@RequestParam String nom) {
        try {
            List<Utilisateur> utilisateurs = utilisateurService.getUtilisateursParNom(nom);
            utilisateurs.forEach(u -> u.setMotDePasse(null));
            return ResponseEntity.ok(utilisateurs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la recherche par nom", "message", e.getMessage()));
        }
    }

    // Recherche par rôle - ADMIN seulement
    @GetMapping("/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUtilisateursParRole(@RequestParam Role role) {
        try {
            List<Utilisateur> utilisateurs = utilisateurService.getUtilisateursParRole(role);
            utilisateurs.forEach(u -> u.setMotDePasse(null));
            return ResponseEntity.ok(utilisateurs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la recherche par rôle", "message", e.getMessage()));
        }
    }

    // Compter les utilisateurs par rôle - ADMIN seulement
    @GetMapping("/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> countByRole(@RequestParam Role role) {
        try {
            Long count = utilisateurService.countByRole(role);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors du comptage", "message", e.getMessage()));
        }
    }

    // Supprimer un utilisateur - ADMIN seulement
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUtilisateur(@PathVariable Long id) {
        try {
            Optional<Utilisateur> utilisateur = utilisateurService.getUtilisateurById(id);
            if (utilisateur.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            utilisateurService.deleteUtilisateur(id);
            return ResponseEntity.ok(Map.of("message", "Utilisateur supprimé avec succès"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la suppression", "message", e.getMessage()));
        }
    }

    // ===== GESTION DU PROFIL PERSONNEL - TOUS LES UTILISATEURS CONNECTÉS =====

    // NOUVEAU : Obtenir son propre profil - Tous les utilisateurs connectés
    @GetMapping("/mon-profil")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMonProfil() {
        try {
            Utilisateur currentUser = getCurrentUser();
            // Ne pas retourner le mot de passe
            currentUser.setMotDePasse(null);
            return ResponseEntity.ok(currentUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération du profil", "message", e.getMessage()));
        }
    }

    // NOUVEAU : Modifier son propre profil (nom et email seulement) - Tous les utilisateurs connectés
    @PutMapping("/mon-profil")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateMonProfil(@RequestBody Map<String, String> updates) {
        try {
            Utilisateur currentUser = getCurrentUser();

            // L'utilisateur ne peut modifier que son nom et email, PAS son rôle
            if (updates.containsKey("nom")) {
                currentUser.setNom(updates.get("nom"));
            }
            if (updates.containsKey("email")) {
                // Vérifier que le nouvel email n'existe pas déjà
                String nouvelEmail = updates.get("email");
                if (!nouvelEmail.equals(currentUser.getEmail()) &&
                        utilisateurService.getByEmail(nouvelEmail).isPresent()) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Email déjà utilisé", "message", "Cet email est déjà utilisé par un autre utilisateur"));
                }
                currentUser.setEmail(nouvelEmail);
            }

            Utilisateur updatedUser = utilisateurService.saveUtilisateur(currentUser);

            // Ne pas retourner le mot de passe
            updatedUser.setMotDePasse(null);

            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la mise à jour du profil", "message", e.getMessage()));
        }
    }

    // NOUVEAU : Changer son propre mot de passe - Tous les utilisateurs connectés
    @PutMapping("/mon-profil/mot-de-passe")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changerMonMotDePasse(@RequestBody Map<String, String> passwords) {
        try {
            Utilisateur currentUser = getCurrentUser();

            String ancienMotDePasse = passwords.get("ancienMotDePasse");
            String nouveauMotDePasse = passwords.get("nouveauMotDePasse");

            if (ancienMotDePasse == null || nouveauMotDePasse == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Données manquantes", "message", "L'ancien et le nouveau mot de passe sont requis"));
            }

            // Vérifier l'ancien mot de passe
            if (!passwordEncoder.matches(ancienMotDePasse, currentUser.getMotDePasse())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Mot de passe incorrect", "message", "L'ancien mot de passe est incorrect"));
            }

            // Encoder et sauvegarder le nouveau mot de passe
            currentUser.setMotDePasse(passwordEncoder.encode(nouveauMotDePasse));
            utilisateurService.saveUtilisateur(currentUser);

            return ResponseEntity.ok(Map.of("message", "Mot de passe modifié avec succès"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors du changement de mot de passe", "message", e.getMessage()));
        }
    }
}