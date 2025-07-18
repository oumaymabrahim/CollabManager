package com.proxym.collabmanager.repositories;

import com.proxym.collabmanager.entities.Projet;
import com.proxym.collabmanager.enums.StatutProjet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProjetRepository extends JpaRepository<Projet, Long> {

    List<Projet> findByNomContainingIgnoreCase(String nom);

    List<Projet> findByStatut(StatutProjet statut);

    List<Projet> findByDateCreationBetween(LocalDate dateDebut, LocalDate dateFin);

    Long countByStatut(StatutProjet statut);

    @Query("SELECT p FROM Projet p JOIN p.participants u WHERE u.id = :utilisateurId")
    List<Projet> findByParticipantId(@Param("utilisateurId") Long utilisateurId);

    @Query("SELECT p FROM Projet p WHERE p.dateEcheance BETWEEN :dateDebut AND :dateFin AND p.statut != 'TERMINE'")
    List<Projet> findProjetsProcheEcheance(@Param("dateDebut") LocalDate dateDebut,
                                           @Param("dateFin") LocalDate dateFin);

    @Query("SELECT p FROM Projet p WHERE p.dateEcheance < :dateActuelle AND p.statut NOT IN ('TERMINE', 'ANNULE')")
    List<Projet> findProjetsEnRetard(@Param("dateActuelle") LocalDate dateActuelle);

    List<Projet> findByBudgetGreaterThan(Double budget);

    // ⚠️ Méthode complexe supprimée : à calculer côté service pour éviter erreur 'SELECT unexpected'
}
