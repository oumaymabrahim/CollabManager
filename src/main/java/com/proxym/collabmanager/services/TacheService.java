package com.proxym.collabmanager.services;

import com.proxym.collabmanager.entities.Tache;
import com.proxym.collabmanager.enums.Priorite;
import com.proxym.collabmanager.enums.StatutTache;
import com.proxym.collabmanager.repositories.TacheRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TacheService {

    private final TacheRepository tacheRepository;

    //  Créer ou mettre à jour une tâche
    public Tache saveTache(Tache tache) {
        return tacheRepository.save(tache);
    }

    //  Supprimer une tâche
    public void deleteTache(Long id) {
        tacheRepository.deleteById(id);
    }

    //  Obtenir une tâche par ID
    public Optional<Tache> getTacheById(Long id) {
        return tacheRepository.findById(id);
    }

    //  Obtenir toutes les tâches
    public List<Tache> getAllTaches() {
        return tacheRepository.findAll();
    }

    //  Recherche par utilisateur
    public List<Tache> getTachesByUtilisateur(Long utilisateurId) {
        return tacheRepository.findByUtilisateurId(utilisateurId);
    }

    //  Recherche par projet
    public List<Tache> getTachesByProjet(Long projetId) {
        return tacheRepository.findByProjetId(projetId);
    }

    //  Recherche par statut
    public List<Tache> getTachesByStatut(StatutTache statut) {
        return tacheRepository.findByStatut(statut);
    }

    //  Recherche par priorité
    public List<Tache> getTachesByPriorite(Priorite priorite) {
        return tacheRepository.findByPriorite(priorite);
    }

    // Tâches d’un utilisateur dans un projet
    public List<Tache> getTachesByUtilisateurAndProjet(Long utilisateurId, Long projetId) {
        return tacheRepository.findByUtilisateurIdAndProjetId(utilisateurId, projetId);
    }

    //  Tâches proches de l’échéance
    public List<Tache> getTachesProcheEcheance(LocalDate debut, LocalDate fin) {
        return tacheRepository.findTachesProcheEcheance(debut, fin);
    }

    //  Tâches en retard
    public List<Tache> getTachesEnRetard(LocalDate dateActuelle) {
        return tacheRepository.findTachesEnRetard(dateActuelle);
    }

    //  Tâches non assignées
    public List<Tache> getTachesNonAssignees() {
        return tacheRepository.findTachesNonAssignees();
    }

    // Tâches haute priorité
    public List<Tache> getTachesHautePriorite() {
        return tacheRepository.findTachesHautePriorite();
    }

    //  Recherche par période
    public List<Tache> getTachesByDateCreationBetween(LocalDate debut, LocalDate fin) {
        return tacheRepository.findByDateCreationBetween(debut, fin);
    }

    //  Compter les tâches par statut et projet
    public Long countTachesByProjetAndStatut(Long projetId, StatutTache statut) {
        return tacheRepository.countByProjetIdAndStatut(projetId, statut);
    }

    //  Compter les tâches par statut et utilisateur
    public Long countTachesByUtilisateurAndStatut(Long utilisateurId, StatutTache statut) {
         return tacheRepository.countByProjetIdAndStatut(utilisateurId, statut); }
    }