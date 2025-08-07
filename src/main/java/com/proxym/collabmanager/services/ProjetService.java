package com.proxym.collabmanager.services;

import com.proxym.collabmanager.entities.Projet;
import com.proxym.collabmanager.enums.Role;
import com.proxym.collabmanager.enums.StatutProjet;
import com.proxym.collabmanager.repositories.ProjetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjetService {

    private final ProjetRepository projetRepository;

    public Projet createProjet(Projet projet) {
        return projetRepository.save(projet);
    }

    public List<Projet> getAllProjets() {
        return projetRepository.findAll();
    }

    public Optional<Projet> getProjetById(Long id) {
        return projetRepository.findById(id);
    }

    public void deleteProjet(Long id) {
        projetRepository.deleteById(id);
    }

    public List<Projet> searchByNom(String nom) {
        return projetRepository.findByNomContainingIgnoreCase(nom);
    }

    public List<Projet> getByStatut(StatutProjet statut) {
        return projetRepository.findByStatut(statut);
    }

    // Obtenir les projets d'un participant (utilisateur assigné)
    public List<Projet> getProjetsByParticipant(Long utilisateurId) {
        return projetRepository.findByParticipantId(utilisateurId);
    }

    // Obtenir les projets d'un participant par statut
    public List<Projet> getProjetsByParticipantAndStatut(Long utilisateurId, StatutProjet statut) {
        return projetRepository.findByParticipantIdAndStatut(utilisateurId, statut);
    }

    // Obtenir les projets où un utilisateur avec le rôle CHEF_DE_PROJECT est participant
    public List<Projet> getProjetsByChefProjet(Long chefProjetId) {
        return projetRepository.findByParticipantId(chefProjetId)
                .stream()
                .filter(projet -> projet.getParticipants().stream()
                        .anyMatch(participant ->
                                participant.getId().equals(chefProjetId) &&
                                        participant.getRole() == Role.CHEF_DE_PROJECT
                        ))
                .collect(Collectors.toList());
    }

    // Obtenir les projets actifs (EN_COURS) d'un utilisateur
    public List<Projet> getProjetsActifsByParticipant(Long utilisateurId) {
        return projetRepository.findByParticipantIdAndStatut(utilisateurId, StatutProjet.EN_COURS);
    }

    // Vérifier si un utilisateur est participant d'un projet
    public boolean isUserParticipantOfProject(Long utilisateurId, Long projetId) {
        return projetRepository.existsByIdAndParticipantsId(projetId, utilisateurId);
    }

    // Obtenir les statistiques d'un projet
    public java.util.Map<String, Object> getProjetStatistics(Long projetId) {
        Optional<Projet> projetOpt = projetRepository.findById(projetId);
        if (projetOpt.isEmpty()) {
            return java.util.Map.of("error", "Projet non trouvé");
        }

        Projet projet = projetOpt.get();
        int nombreParticipants = projet.getParticipants().size();
        int nombreTaches = projet.getTaches() != null ? projet.getTaches().size() : 0;

        return java.util.Map.of(
                "id", projet.getId(),
                "nom", projet.getNom(),
                "statut", projet.getStatut(),
                "nombreParticipants", nombreParticipants,
                "nombreTaches", nombreTaches,
                "dateCreation", projet.getDateCreation(),
                "dateEcheance", projet.getDateEcheance(),
                "budget", projet.getBudget() != null ? projet.getBudget() : 0.0
        );
    }

    // Obtenir les projets en retard
    public List<Projet> getProjetsEnRetard() {
        return projetRepository.findOverdueProjects();
    }

    // Obtenir les projets sans participants
    public List<Projet> getProjetsSansParticipants() {
        return projetRepository.findProjectsWithoutParticipants();
    }

    // Compter les projets par statut
    public long countByStatut(StatutProjet statut) {
        return projetRepository.countByStatut(statut);
    }

    // Obtenir les statistiques globales
    public java.util.Map<String, Object> getStatistiquesGlobales() {
        long totalProjets = projetRepository.count();
        long projetsActifs = countByStatut(StatutProjet.EN_COURS);
        long projetsTermines = countByStatut(StatutProjet.TERMINE);
        long projetsPlanifies = countByStatut(StatutProjet.PLANIFIE);
        long projetsSuspendus = countByStatut(StatutProjet.SUSPENDU);
        long projetsAnnules = countByStatut(StatutProjet.ANNULE);

        List<Projet> projetsEnRetard = getProjetsEnRetard();
        List<Projet> projetsSansParticipants = getProjetsSansParticipants();

        return java.util.Map.of(
                "totalProjets", totalProjets,
                "repartitionParStatut", java.util.Map.of(
                        "PLANIFIE", projetsPlanifies,
                        "EN_COURS", projetsActifs,
                        "TERMINE", projetsTermines,
                        "SUSPENDU", projetsSuspendus,
                        "ANNULE", projetsAnnules
                ),
                "projetsEnRetard", projetsEnRetard.size(),
                "projetsSansParticipants", projetsSansParticipants.size(),
                "dateCalcul", LocalDate.now()
        );
    }
}
