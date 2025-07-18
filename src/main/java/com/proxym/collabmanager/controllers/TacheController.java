package com.proxym.collabmanager.controllers;

import com.proxym.collabmanager.entities.Tache;
import com.proxym.collabmanager.enums.Priorite;
import com.proxym.collabmanager.enums.StatutTache;
import com.proxym.collabmanager.services.TacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/taches")
@RequiredArgsConstructor
public class TacheController {

    private final TacheService tacheService;

    // Créer ou mettre à jour une tâche
    @PostMapping
    public Tache saveTache(@RequestBody Tache tache) {
        return tacheService.saveTache(tache);
    }

    //  Supprimer une tâche
    @DeleteMapping("/{id}")
    public void deleteTache(@PathVariable Long id) {
        tacheService.deleteTache(id);
    }

    //  Obtenir toutes les tâches
    @GetMapping
    public List<Tache> getAllTaches() {
        return tacheService.getAllTaches();
    }

    //  Obtenir une tâche par ID
    @GetMapping("/{id}")
    public Tache getTacheById(@PathVariable Long id) {
        return tacheService.getTacheById(id).orElse(null);
    }

    //  Tâches par utilisateur
    @GetMapping("/utilisateur/{id}")
    public List<Tache> getTachesByUtilisateur(@PathVariable Long id) {
        return tacheService.getTachesByUtilisateur(id);
    }

    //  Tâches par projet
    @GetMapping("/projet/{id}")
    public List<Tache> getTachesByProjet(@PathVariable Long id) {
        return tacheService.getTachesByProjet(id);
    }

    //  Tâches par statut
    @GetMapping("/statut")
    public List<Tache> getTachesByStatut(@RequestParam StatutTache statut) {
        return tacheService.getTachesByStatut(statut);
    }

    // Tâches par priorité
    @GetMapping("/priorite")
    public List<Tache> getTachesByPriorite(@RequestParam Priorite priorite) {
        return tacheService.getTachesByPriorite(priorite);
    }

    //  Tâches d’un utilisateur dans un projet
    @GetMapping("/utilisateur-projet")
    public List<Tache> getTachesByUtilisateurAndProjet(@RequestParam Long utilisateurId, @RequestParam Long projetId) {
        return tacheService.getTachesByUtilisateurAndProjet(utilisateurId, projetId);
    }

    //  Tâches proches de l’échéance
    @GetMapping("/proche-echeance")
    public List<Tache> getTachesProcheEcheance(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        return tacheService.getTachesProcheEcheance(debut, fin);
    }

    // Tâches en retard
    @GetMapping("/en-retard")
    public List<Tache> getTachesEnRetard(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return tacheService.getTachesEnRetard(date);
    }

    //  Tâches non assignées
    @GetMapping("/non-assignees")
    public List<Tache> getTachesNonAssignees() {
        return tacheService.getTachesNonAssignees();
    }

    //  Tâches à haute priorité
    @GetMapping("/haute-priorite")
    public List<Tache> getTachesHautePriorite() {
        return tacheService.getTachesHautePriorite();
    }

    //  Tâches créées entre deux dates
    @GetMapping("/entre-dates")
    public List<Tache> getTachesByDateCreationBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        return tacheService.getTachesByDateCreationBetween(debut, fin);
    }

    //  Compter les tâches d’un projet par statut
    @GetMapping("/count/projet")
    public Long countTachesByProjetAndStatut(@RequestParam Long projetId, @RequestParam StatutTache statut) {
        return tacheService.countTachesByProjetAndStatut(projetId, statut);
    }

    //  Compter les tâches d’un utilisateur par statut
    @GetMapping("/count/utilisateur")
    public Long countTachesByUtilisateurAndStatut(@RequestParam Long utilisateurId, @RequestParam StatutTache statut) {
        return tacheService.countTachesByUtilisateurAndStatut(utilisateurId, statut);
    }
}
