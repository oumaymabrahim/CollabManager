package com.proxym.collabmanager.services;

import com.proxym.collabmanager.entities.Tache;
import com.proxym.collabmanager.enums.StatutTache;
import com.proxym.collabmanager.repositories.TacheRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TacheService {

    private final TacheRepository tacheRepository;

    // Créer ou mettre à jour une tâche
    public Tache saveTache(Tache tache) {
        return tacheRepository.save(tache);
    }

    // Supprimer une tâche
    public void deleteTache(Long id) {
        tacheRepository.deleteById(id);
    }

    // Obtenir une tâche par ID
    public Optional<Tache> getTacheById(Long id) {
        return tacheRepository.findById(id);
    }

    // Obtenir toutes les tâches
    public List<Tache> getAllTaches() {
        return tacheRepository.findAll();
    }

    // Recherche par utilisateur
    public List<Tache> getTachesByUtilisateur(Long utilisateurId) {
        return tacheRepository.findByUtilisateurId(utilisateurId);
    }

    // Recherche par projet
    public List<Tache> getTachesByProjet(Long projetId) {
        return tacheRepository.findByProjetId(projetId);
    }

    // Recherche par statut
    public List<Tache> getTachesByStatut(StatutTache statut) {
        return tacheRepository.findByStatut(statut);
    }

    // NOUVEAU : Recherche par statut ET utilisateur (pour les membres d'équipe)
    public List<Tache> getTachesByStatutAndUtilisateur(StatutTache statut, Long utilisateurId) {
        return tacheRepository.findByStatutAndUtilisateurId(statut, utilisateurId);
    }

    // NOUVEAU : Compter les tâches par projet et statut
    public Long countTachesByProjetAndStatut(Long projetId, StatutTache statut) {
        return tacheRepository.countByProjetIdAndStatut(projetId, statut);
    }

    // NOUVEAU : Compter les tâches par utilisateur et statut
    public Long countTachesByUtilisateurAndStatut(Long utilisateurId, StatutTache statut) {
        return tacheRepository.countByUtilisateurIdAndStatut(utilisateurId, statut);
    }

    // NOUVEAU : Obtenir les tâches d'un utilisateur pour un projet spécifique
    public List<Tache> getTachesByUtilisateurAndProjet(Long utilisateurId, Long projetId) {
        return tacheRepository.findByUtilisateurIdAndProjetId(utilisateurId, projetId);
    }
}