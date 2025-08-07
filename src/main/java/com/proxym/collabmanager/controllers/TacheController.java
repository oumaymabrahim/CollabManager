package com.proxym.collabmanager.controllers;

import com.proxym.collabmanager.entities.Tache;
import com.proxym.collabmanager.entities.Utilisateur;
import com.proxym.collabmanager.enums.Role;
import com.proxym.collabmanager.enums.StatutTache;
import com.proxym.collabmanager.services.TacheService;
import com.proxym.collabmanager.services.UtilisateurService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/taches")
@RequiredArgsConstructor
public class TacheController {

    private final TacheService tacheService;
    private final UtilisateurService utilisateurService;

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

    // CHEF_DE_PROJET uniquement : Créer une tâche
    @PostMapping("/add")
    public ResponseEntity<?> saveTache(@RequestBody Tache tache) {
        Utilisateur currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.CHEF_DE_PROJECT) {
            return createAccessDeniedResponse();
        }

        try {
            Tache savedTache = tacheService.saveTache(tache);
            return ResponseEntity.ok(savedTache);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la création de la tâche", "message", e.getMessage()));
        }
    }

    // CHEF_DE_PROJET uniquement : Modifier une tâche
    @PutMapping("/{id}/update")
    public ResponseEntity<?> updateTache(@PathVariable Long id, @RequestBody Tache tache) {
        Utilisateur currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.CHEF_DE_PROJECT) {
            return createAccessDeniedResponse();
        }

        try {
            Optional<Tache> existingTache = tacheService.getTacheById(id);
            if (existingTache.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            tache.setId(id);
            Tache updatedTache = tacheService.saveTache(tache);
            return ResponseEntity.ok(updatedTache);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la modification de la tâche", "message", e.getMessage()));
        }
    }

    // MEMBRE_EQUIPE : Modifier uniquement le statut de sa propre tâche
    @PutMapping("/{id}/update-statut")
    public ResponseEntity<?> updateTacheStatut(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Utilisateur currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.MEMBRE_EQUIPE) {
            return createAccessDeniedResponse();
        }

        try {
            Optional<Tache> tacheOpt = tacheService.getTacheById(id);
            if (tacheOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Tache tache = tacheOpt.get();

            // Vérifier que la tâche appartient à l'utilisateur connecté
            if (!tache.getUtilisateur().getId().equals(currentUser.getId())) {
                return createAccessDeniedResponse();
            }

            String nouveauStatut = request.get("statut");
            if (nouveauStatut == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Statut manquant", "message", "Le statut est requis"));
            }

            try {
                StatutTache statut = StatutTache.valueOf(nouveauStatut);
                tache.setStatut(statut);
                Tache updatedTache = tacheService.saveTache(tache);
                return ResponseEntity.ok(updatedTache);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Statut invalide", "message", "Le statut fourni n'est pas valide"));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la mise à jour du statut", "message", e.getMessage()));
        }
    }

    // CHEF_DE_PROJET uniquement : Supprimer une tâche
    @DeleteMapping("/{id}/delete")
    public ResponseEntity<?> deleteTache(@PathVariable Long id) {
        Utilisateur currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.CHEF_DE_PROJECT) {
            return createAccessDeniedResponse();
        }

        try {
            Optional<Tache> tache = tacheService.getTacheById(id);
            if (tache.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            tacheService.deleteTache(id);
            return ResponseEntity.ok(Map.of("message", "Tâche supprimée avec succès"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la suppression", "message", e.getMessage()));
        }
    }

    // CHEF_DE_PROJET uniquement : Obtenir toutes les tâches
    @GetMapping("/all")
    public ResponseEntity<?> getAllTaches() {
        Utilisateur currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.CHEF_DE_PROJECT) {
            return createAccessDeniedResponse();
        }

        try {
            List<Tache> taches = tacheService.getAllTaches();
            return ResponseEntity.ok(taches);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération des tâches", "message", e.getMessage()));
        }
    }

    // CHEF_DE_PROJET uniquement : Obtenir une tâche par ID
    @GetMapping("/{id}/tache")
    public ResponseEntity<?> getTacheById(@PathVariable Long id) {
        Utilisateur currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.CHEF_DE_PROJECT) {
            return createAccessDeniedResponse();
        }

        try {
            Optional<Tache> tache = tacheService.getTacheById(id);
            if (tache.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(tache.get());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération de la tâche", "message", e.getMessage()));
        }
    }

    // MEMBRE_EQUIPE : Obtenir ses propres tâches uniquement
    @GetMapping("/mes-taches")
    public ResponseEntity<?> getMesTaches() {
        Utilisateur currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.MEMBRE_EQUIPE) {
            return createAccessDeniedResponse();
        }

        try {
            List<Tache> taches = tacheService.getTachesByUtilisateur(currentUser.getId());
            return ResponseEntity.ok(taches);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération de vos tâches", "message", e.getMessage()));
        }
    }

    // CHEF_DE_PROJET et MEMBRE_EQUIPE : Consulter le statut des tâches (CORRIGÉ)
    @GetMapping("/statut")
    public ResponseEntity<?> getTachesByStatut(@RequestParam StatutTache statut) {
        Utilisateur currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.CHEF_DE_PROJECT && currentUser.getRole() != Role.MEMBRE_EQUIPE) {
            return createAccessDeniedResponse();
        }

        try {
            List<Tache> taches;

            if (currentUser.getRole() == Role.CHEF_DE_PROJECT) {
                // Chef voit toutes les tâches avec ce statut
                taches = tacheService.getTachesByStatut(statut);
            } else {
                // Membre voit seulement ses propres tâches avec ce statut
                taches = tacheService.getTachesByStatutAndUtilisateur(statut, currentUser.getId());
            }

            return ResponseEntity.ok(taches);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération des tâches", "message", e.getMessage()));
        }
    }

    // CHEF_DE_PROJET uniquement : Tâches par projet
    @GetMapping("/projet/{id}")
    public ResponseEntity<?> getTachesByProjet(@PathVariable Long id) {
        Utilisateur currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.CHEF_DE_PROJECT) {
            return createAccessDeniedResponse();
        }

        try {
            List<Tache> taches = tacheService.getTachesByProjet(id);
            return ResponseEntity.ok(taches);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération des tâches du projet", "message", e.getMessage()));
        }
    }

    // NOUVEAU : CHEF_DE_PROJET uniquement : Obtenir les tâches assignées à un utilisateur spécifique
    @GetMapping("/utilisateur/{utilisateurId}")
    public ResponseEntity<?> getTachesAssigneesUtilisateur(@PathVariable Long utilisateurId) {
        Utilisateur currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.CHEF_DE_PROJECT) {
            return createAccessDeniedResponse();
        }

        try {
            // Vérifier que l'utilisateur existe
            Optional<Utilisateur> utilisateur = utilisateurService.getUtilisateurById(utilisateurId);
            if (utilisateur.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Utilisateur non trouvé", "message", "L'utilisateur avec l'ID " + utilisateurId + " n'existe pas"));
            }

            List<Tache> taches = tacheService.getTachesByUtilisateur(utilisateurId);
            return ResponseEntity.ok(Map.of(
                    "utilisateur", utilisateur.get().getNom() + " (" + utilisateur.get().getEmail() + ")",
                    "taches", taches,
                    "nombreTaches", taches.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération des tâches assignées", "message", e.getMessage()));
        }
    }

    // NOUVEAU : CHEF_DE_PROJET uniquement : Obtenir les tâches assignées à un utilisateur par statut
    @GetMapping("/utilisateur/{utilisateurId}/statut")
    public ResponseEntity<?> getTachesAssigneesUtilisateurParStatut(
            @PathVariable Long utilisateurId,
            @RequestParam StatutTache statut) {
        Utilisateur currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.CHEF_DE_PROJECT) {
            return createAccessDeniedResponse();
        }

        try {
            // Vérifier que l'utilisateur existe
            Optional<Utilisateur> utilisateur = utilisateurService.getUtilisateurById(utilisateurId);
            if (utilisateur.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Utilisateur non trouvé", "message", "L'utilisateur avec l'ID " + utilisateurId + " n'existe pas"));
            }

            List<Tache> taches = tacheService.getTachesByStatutAndUtilisateur(statut, utilisateurId);
            return ResponseEntity.ok(Map.of(
                    "utilisateur", utilisateur.get().getNom() + " (" + utilisateur.get().getEmail() + ")",
                    "statut", statut,
                    "taches", taches,
                    "nombreTaches", taches.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération des tâches assignées par statut", "message", e.getMessage()));
        }
    }
}