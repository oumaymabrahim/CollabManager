package com.proxym.collabmanager.repositories;

import com.proxym.collabmanager.entities.Tache;
import com.proxym.collabmanager.enums.Priorite;
import com.proxym.collabmanager.enums.StatutTache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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
    List<Tache> findByDateCreationBetween(LocalDate dateDebut, LocalDate dateFin);

    Long countByProjetIdAndStatut(Long projetId, StatutTache statut);
    Long countByUtilisateurIdAndStatut(Long utilisateurId, StatutTache statut);

    @Query("SELECT t FROM Tache t WHERE t.dateEcheance BETWEEN :dateDebut AND :dateFin AND t.statut != 'TERMINEE'")
    List<Tache> findTachesProcheEcheance(@Param("dateDebut") LocalDate dateDebut,
                                         @Param("dateFin") LocalDate dateFin);

    @Query("SELECT t FROM Tache t WHERE t.dateEcheance < :dateActuelle AND t.statut NOT IN ('TERMINEE', 'ANNULEE')")
    List<Tache> findTachesEnRetard(@Param("dateActuelle") LocalDate dateActuelle);

    @Query("SELECT t FROM Tache t WHERE t.utilisateur IS NULL")
    List<Tache> findTachesNonAssignees();

    @Query("SELECT t FROM Tache t WHERE t.priorite IN ('HAUTE', 'URGENTE') AND t.statut != 'TERMINEE'")
    List<Tache> findTachesHautePriorite();

}