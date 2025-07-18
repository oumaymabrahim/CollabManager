package com.proxym.collabmanager.controllers;

import com.proxym.collabmanager.entities.Utilisateur;
import com.proxym.collabmanager.enums.Role;
import com.proxym.collabmanager.services.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    //  Créer ou mettre à jour un utilisateur
    @PostMapping
    public Utilisateur saveUtilisateur(@RequestBody Utilisateur utilisateur) {
        return utilisateurService.saveUtilisateur(utilisateur);
    }

    //  Supprimer un utilisateur
    @DeleteMapping("/{id}")
    public void deleteUtilisateur(@PathVariable Long id) {
        utilisateurService.deleteUtilisateur(id);
    }

    // Obtenir un utilisateur par ID
    @GetMapping("/{id}")
    public Optional<Utilisateur> getUtilisateurById(@PathVariable Long id) {
        return utilisateurService.getUtilisateurById(id);
    }

    //  Obtenir tous les utilisateurs
    @GetMapping
    public List<Utilisateur> getAllUtilisateurs() {
        return utilisateurService.getAllUtilisateurs();
    }

    //  Recherche par email
    @GetMapping("/email")
    public Optional<Utilisateur> getByEmail(@RequestParam String email) {
        return utilisateurService.getByEmail(email);
    }

    //  Vérifier si email existe
    @GetMapping("/email/existe")
    public boolean emailExiste(@RequestParam String email) {
        return utilisateurService.emailExiste(email);
    }

    //  Recherche par nom
    @GetMapping("/recherche")
    public List<Utilisateur> getUtilisateursParNom(@RequestParam String nom) {
        return utilisateurService.getUtilisateursParNom(nom);
    }

    //  Recherche par rôle
    @GetMapping("/role")
    public List<Utilisateur> getUtilisateursParRole(@RequestParam Role role) {
        return utilisateurService.getUtilisateursParRole(role);
    }

    //  Compter les utilisateurs par rôle
    @GetMapping("/count")
    public Long countByRole(@RequestParam Role role) {
        return utilisateurService.countByRole(role);
    }

    //  Utilisateurs d’un projet
    @GetMapping("/par-projet/{projetId}")
    public List<Utilisateur> getUtilisateursParProjet(@PathVariable Long projetId) {
        return utilisateurService.getUtilisateursParProjet(projetId);
    }

    //  Utilisateurs avec des tâches dans un projet
    @GetMapping("/taches-par-projet/{projetId}")
    public List<Utilisateur> getUtilisateursAvecTachesDansProjet(@PathVariable Long projetId) {
        return utilisateurService.getUtilisateursAvecTachesDansProjet(projetId);
    }

    //  Utilisateurs actifs (ayant des tâches en cours)
    @GetMapping("/actifs")
    public List<Utilisateur> getUtilisateursActifs() {
        return utilisateurService.getUtilisateursActifs();
    }
}
