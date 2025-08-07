package com.proxym.collabmanager.controllers;

import com.proxym.collabmanager.entities.Projet;
import com.proxym.collabmanager.entities.Utilisateur;
import com.proxym.collabmanager.enums.Role;
import com.proxym.collabmanager.enums.StatutProjet;
import com.proxym.collabmanager.services.ProjetService;
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
@RequestMapping("/api/projets")
@RequiredArgsConstructor
public class ProjetController {

    private final ProjetService projetService;
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

    // ADMIN uniquement : Créer un projet
    @PostMapping("/add")
    public ResponseEntity<?> createProjet(@RequestBody Projet projet) {
        Utilisateur currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.ADMIN) {
            return createAccessDeniedResponse();
        }

        try {
            Projet savedProjet = projetService.createProjet(projet);
            return ResponseEntity.ok(savedProjet);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la création du projet", "message", e.getMessage()));
        }
    }

    // ADMIN : Voir tous les projets
    @GetMapping("/all")
    public ResponseEntity<?> getAllProjets() {
        Utilisateur currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.ADMIN) {
            return createAccessDeniedResponse();
        }

        try {
            List<Projet> projets = projetService.getAllProjets();
            return ResponseEntity.ok(projets);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération des projets", "message", e.getMessage()));
        }
    }

    // NOUVEAU : CHEF_DE_PROJECT et MEMBRE_EQUIPE : Voir ses projets assignés
    @GetMapping("/mes-projets")
    public ResponseEntity<?> getMesProjets() {
        Utilisateur currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.CHEF_DE_PROJECT && currentUser.getRole() != Role.MEMBRE_EQUIPE) {
            return createAccessDeniedResponse();
        }

        try {
            List<Projet> projets = projetService.getProjetsByParticipant(currentUser.getId());
            return ResponseEntity.ok(projets);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération de vos projets", "message", e.getMessage()));
        }
    }

    // Tous les rôles : Voir un projet par ID (avec contrôles d'accès)
    @GetMapping("/{id}/projet")
    public ResponseEntity<?> getProjetById(@PathVariable Long id) {
        Utilisateur currentUser = getCurrentUser();

        try {
            Optional<Projet> projetOpt = projetService.getProjetById(id);
            if (projetOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Projet projet = projetOpt.get();

            // ADMIN peut voir tous les projets
            if (currentUser.getRole() == Role.ADMIN) {
                return ResponseEntity.ok(projet);
            }

            // CHEF_DE_PROJECT et MEMBRE_EQUIPE peuvent voir seulement leurs projets assignés
            boolean isParticipant = projet.getParticipants().stream()
                    .anyMatch(participant -> participant.getId().equals(currentUser.getId()));

            if (!isParticipant) {
                return createAccessDeniedResponse();
            }

            return ResponseEntity.ok(projet);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération du projet", "message", e.getMessage()));
        }
    }

    // ADMIN uniquement : Supprimer un projet
    @DeleteMapping("/{id}/delete")
    public ResponseEntity<?> deleteProjet(@PathVariable Long id) {
        Utilisateur currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.ADMIN) {
            return createAccessDeniedResponse();
        }

        try {
            Optional<Projet> projet = projetService.getProjetById(id);
            if (projet.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            projetService.deleteProjet(id);
            return ResponseEntity.ok(Map.of("message", "Projet supprimé avec succès"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la suppression", "message", e.getMessage()));
        }
    }

    // ADMIN : Rechercher des projets par nom
    @GetMapping("/search")
    public ResponseEntity<?> searchByNom(@RequestParam String nom) {
        Utilisateur currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.ADMIN) {
            return createAccessDeniedResponse();
        }

        try {
            List<Projet> projets = projetService.searchByNom(nom);
            return ResponseEntity.ok(projets);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la recherche", "message", e.getMessage()));
        }
    }

    // Tous les rôles : Voir projets par statut (avec contrôles d'accès)
    @GetMapping("/statut")
    public ResponseEntity<?> getByStatut(@RequestParam StatutProjet statut) {
        Utilisateur currentUser = getCurrentUser();

        try {
            List<Projet> projets;

            if (currentUser.getRole() == Role.ADMIN) {
                // ADMIN voit tous les projets avec ce statut
                projets = projetService.getByStatut(statut);
            } else {
                // CHEF_DE_PROJECT et MEMBRE_EQUIPE voient seulement leurs projets avec ce statut
                projets = projetService.getProjetsByParticipantAndStatut(currentUser.getId(), statut);
            }

            return ResponseEntity.ok(projets);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération des projets", "message", e.getMessage()));
        }
    }

    // NOUVEAU : ADMIN uniquement - Assigner un utilisateur à un projet
    @PostMapping("/{projetId}/assigner/{utilisateurId}")
    public ResponseEntity<?> assignerUtilisateurAuProjet(
            @PathVariable Long projetId,
            @PathVariable Long utilisateurId) {
        Utilisateur currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.ADMIN) {
            return createAccessDeniedResponse();
        }

        try {
            Optional<Projet> projetOpt = projetService.getProjetById(projetId);
            Optional<Utilisateur> utilisateurOpt = utilisateurService.getUtilisateurById(utilisateurId);

            if (projetOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Projet non trouvé"));
            }

            if (utilisateurOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Utilisateur non trouvé"));
            }

            Projet projet = projetOpt.get();
            Utilisateur utilisateur = utilisateurOpt.get();

            // Vérifier si l'utilisateur est déjà assigné
            boolean dejaAssigne = projet.getParticipants().stream()
                    .anyMatch(p -> p.getId().equals(utilisateurId));

            if (dejaAssigne) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "L'utilisateur est déjà assigné à ce projet"));
            }

            projet.getParticipants().add(utilisateur);
            Projet updatedProjet = projetService.createProjet(projet);

            return ResponseEntity.ok(Map.of(
                    "message", "Utilisateur assigné avec succès au projet",
                    "projet", updatedProjet.getNom(),
                    "utilisateur", utilisateur.getNom()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de l'assignation", "message", e.getMessage()));
        }
    }

    // NOUVEAU : ADMIN uniquement - Retirer un utilisateur d'un projet
    @DeleteMapping("/{projetId}/retirer/{utilisateurId}")
    public ResponseEntity<?> retirerUtilisateurDuProjet(
            @PathVariable Long projetId,
            @PathVariable Long utilisateurId) {
        Utilisateur currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.ADMIN) {
            return createAccessDeniedResponse();
        }

        try {
            Optional<Projet> projetOpt = projetService.getProjetById(projetId);
            Optional<Utilisateur> utilisateurOpt = utilisateurService.getUtilisateurById(utilisateurId);

            if (projetOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Projet non trouvé"));
            }

            if (utilisateurOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Utilisateur non trouvé"));
            }

            Projet projet = projetOpt.get();
            Utilisateur utilisateur = utilisateurOpt.get();

            boolean removed = projet.getParticipants().removeIf(p -> p.getId().equals(utilisateurId));

            if (!removed) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "L'utilisateur n'était pas assigné à ce projet"));
            }

            Projet updatedProjet = projetService.createProjet(projet);

            return ResponseEntity.ok(Map.of(
                    "message", "Utilisateur retiré avec succès du projet",
                    "projet", updatedProjet.getNom(),
                    "utilisateur", utilisateur.getNom()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors du retrait", "message", e.getMessage()));
        }
    }

    // NOUVEAU : Voir les participants d'un projet (avec contrôles d'accès)
    @GetMapping("/{id}/participants")
    public ResponseEntity<?> getParticipantsProjet(@PathVariable Long id) {
        Utilisateur currentUser = getCurrentUser();

        try {
            Optional<Projet> projetOpt = projetService.getProjetById(id);
            if (projetOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Projet projet = projetOpt.get();

            // ADMIN peut voir les participants de tous les projets
            if (currentUser.getRole() == Role.ADMIN) {
                return ResponseEntity.ok(Map.of(
                        "projet", projet.getNom(),
                        "participants", projet.getParticipants()
                ));
            }

            // CHEF_DE_PROJECT et MEMBRE_EQUIPE peuvent voir les participants seulement de leurs projets
            boolean isParticipant = projet.getParticipants().stream()
                    .anyMatch(participant -> participant.getId().equals(currentUser.getId()));

            if (!isParticipant) {
                return createAccessDeniedResponse();
            }

            return ResponseEntity.ok(Map.of(
                    "projet", projet.getNom(),
                    "participants", projet.getParticipants()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération des participants", "message", e.getMessage()));
        }
    }
}