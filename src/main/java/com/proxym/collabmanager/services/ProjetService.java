package com.proxym.collabmanager.services;

import com.proxym.collabmanager.entities.Projet;
import com.proxym.collabmanager.entities.Tache;
import com.proxym.collabmanager.enums.StatutProjet;
import com.proxym.collabmanager.enums.StatutTache;
import com.proxym.collabmanager.repositories.ProjetRepository;
import com.proxym.collabmanager.repositories.TacheRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ProjetService {

    private final ProjetRepository projetRepository;
    private final TacheRepository tacheRepository;

    public ProjetService(ProjetRepository projetRepository, TacheRepository tacheRepository) {
        this.projetRepository = projetRepository;
        this.tacheRepository = tacheRepository;
    }

    public Projet createProjet(Projet projet) {
        return projetRepository.save(projet);
    }

    public List<Projet> getAllProjets() {
        return projetRepository.findAll();
    }

    public Projet getProjetById(Long id) {
        return projetRepository.findById(id).orElse(null);
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

    public List<Projet> getProjetsParParticipant(Long utilisateurId) {
        return projetRepository.findByParticipantId(utilisateurId);
    }

    public Double getAvancementProjet(Long projetId) {
        List<Tache> toutes = tacheRepository.findByProjetId(projetId);
        long total = toutes.size();
        long terminees = toutes.stream()
                .filter(t -> t.getStatut() == StatutTache.TERMINEE)
                .count();
        return total == 0 ? 0.0 : (terminees * 100.0) / total;
    }
}