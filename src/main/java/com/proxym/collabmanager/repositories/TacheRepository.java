package com.proxym.collabmanager.repositories;

import com.proxym.collabmanager.entities.Tache;
import com.proxym.collabmanager.enums.Priorite;
import com.proxym.collabmanager.enums.StatutTache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface TacheRepository extends JpaRepository<Tache, Long> {

    List<Tache> findByNomContainingIgnoreCase(String nom);
    List<Tache> findByStatut(StatutTache statut);
    List<Tache> findByPriorite(Priorite priorite);
    List<Tache> findByUtilisateurId(Long utilisateurId);
    List<Tache> findByProjetId(Long projetId);
    List<Tache> findByUtilisateurIdAndProjetId(Long utilisateurId, Long projetId);
    List<Tache> findByStatutAndUtilisateurId(StatutTache statut, Long utilisateurId);


    Long countByProjetIdAndStatut(Long projetId, StatutTache statut);
    Long countByUtilisateurIdAndStatut(Long utilisateurId, StatutTache statut);



}