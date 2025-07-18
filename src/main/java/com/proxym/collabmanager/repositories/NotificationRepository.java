package com.proxym.collabmanager.repositories;

import com.proxym.collabmanager.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Notifications d’un utilisateur, triées par date décroissante
    List<Notification> findByUtilisateurIdOrderByDateEnvoiDesc(Long utilisateurId);

    // Notifications non lues
    List<Notification> findByUtilisateurIdAndLueFalseOrderByDateEnvoiDesc(Long utilisateurId);

    // Notifications lues
    List<Notification> findByUtilisateurIdAndLueTrueOrderByDateEnvoiDesc(Long utilisateurId);

    // Notifications par type
    List<Notification> findByTypeAndUtilisateurIdOrderByDateEnvoiDesc(String type, Long utilisateurId);

    // Notifications dans une période
    List<Notification> findByDateEnvoiBetween(LocalDateTime dateDebut, LocalDateTime dateFin);

    //  Garder cette requête car elle compte les non-lues (pas fournie par défaut)
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.utilisateur.id = :utilisateurId AND n.lue = false")
    Long countNotificationsNonLues(@Param("utilisateurId") Long utilisateurId);

    // Modification en masse → on garde le @Modifying + @Query
    @Modifying
    @Query("UPDATE Notification n SET n.lue = true WHERE n.utilisateur.id = :utilisateurId AND n.lue = false")
    int marquerToutesCommeLues(@Param("utilisateurId") Long utilisateurId);

    //  Suppression conditionnelle (pas fournie par défaut)
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.dateEnvoi < :dateLimit")
    int supprimerAnciennesNotifications(@Param("dateLimit") LocalDateTime dateLimit);

    //  Requête personnalisée pour les dernières 24h
    @Query("SELECT n FROM Notification n WHERE n.dateEnvoi >= :date24hAgo ORDER BY n.dateEnvoi DESC")
    List<Notification> findNotificationsRecentes(@Param("date24hAgo") LocalDateTime date24hAgo);


}