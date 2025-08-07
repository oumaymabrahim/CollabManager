package com.proxym.collabmanager.repositories;

import com.proxym.collabmanager.entities.Projet;
import com.proxym.collabmanager.enums.StatutProjet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjetRepository extends JpaRepository<Projet, Long> {

    // Méthodes existantes
    List<Projet> findByNomContainingIgnoreCase(String nom);

    List<Projet> findByStatut(StatutProjet statut);

    @Query("SELECT p FROM Projet p JOIN p.participants u WHERE u.id = :utilisateurId")
    List<Projet> findByParticipantId(@Param("utilisateurId") Long utilisateurId);

    // Nouvelles méthodes nécessaires pour ProjetService

    @Query("SELECT p FROM Projet p JOIN p.participants u WHERE u.id = :utilisateurId AND p.statut = :statut")
    List<Projet> findByParticipantIdAndStatut(@Param("utilisateurId") Long utilisateurId, @Param("statut") StatutProjet statut);

    // Cette méthode est supprimée car l'entité Projet n'a pas d'attribut chefProjet
    // @Query("SELECT p FROM Projet p WHERE p.chefProjet.id = :chefProjetId")
    // List<Projet> findByChefProjetId(@Param("chefProjetId") Long chefProjetId);

    boolean existsByIdAndParticipantsId(Long projetId, Long participantId);

    @Query("SELECT p FROM Projet p WHERE p.dateEcheance < CURRENT_DATE AND p.statut != 'TERMINE' AND p.statut != 'ANNULE'")
    List<Projet> findOverdueProjects();

    @Query("SELECT p FROM Projet p WHERE SIZE(p.participants) = 0")
    List<Projet> findProjectsWithoutParticipants();

    long countByStatut(StatutProjet statut);
}