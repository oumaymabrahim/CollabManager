package com.proxym.collabmanager.controllers;

import com.proxym.collabmanager.entities.Projet;
import com.proxym.collabmanager.enums.StatutProjet;
import com.proxym.collabmanager.services.ProjetService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projets")
public class ProjetController {

    private final ProjetService projetService;

    public ProjetController(ProjetService projetService) {
        this.projetService = projetService;
    }

    @PostMapping
    public Projet createProjet(@RequestBody Projet projet) {
        return projetService.createProjet(projet);
    }

    @GetMapping
    public List<Projet> getAllProjets() {
        return projetService.getAllProjets();
    }

    @GetMapping("/{id}")
    public Projet getProjetById(@PathVariable Long id) {
        return projetService.getProjetById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteProjet(@PathVariable Long id) {
        projetService.deleteProjet(id);
    }

    @GetMapping("/search")
    public List<Projet> searchByNom(@RequestParam String nom) {
        return projetService.searchByNom(nom);
    }

    @GetMapping("/statut")
    public List<Projet> getByStatut(@RequestParam StatutProjet statut) {
        return projetService.getByStatut(statut);
    }

    @GetMapping("/{id}/avancement")
    public Double getAvancement(@PathVariable Long id) {
        return projetService.getAvancementProjet(id);
    }
}
