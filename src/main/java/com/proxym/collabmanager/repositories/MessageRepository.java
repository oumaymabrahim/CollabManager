package com.proxym.collabmanager.repositories;

import com.proxym.collabmanager.entities.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

   @Query("SELECT m FROM Message m WHERE " +
           "(m.expediteur.id = :utilisateur1Id AND m.destinataire.id = :utilisateur2Id) OR " +
           "(m.expediteur.id = :utilisateur2Id AND m.destinataire.id = :utilisateur1Id) " +
           "ORDER BY m.dateEnvoi ASC")
   List<Message> findConversation(@Param("utilisateur1Id") Long utilisateur1Id,
                                  @Param("utilisateur2Id") Long utilisateur2Id);

   // Compter les messages échangés entre deux utilisateurs
   @Query("SELECT COUNT(m) FROM Message m WHERE " +
           "(m.expediteur.id = :utilisateur1Id AND m.destinataire.id = :utilisateur2Id) OR " +
           "(m.expediteur.id = :utilisateur2Id AND m.destinataire.id = :utilisateur1Id)")
   Long countConversation(@Param("utilisateur1Id") Long utilisateur1Id,
                          @Param("utilisateur2Id") Long utilisateur2Id);

   // Derniers messages échangés avec chaque contact
   @Query("SELECT m FROM Message m WHERE m.id IN (" +
           "SELECT MAX(m2.id) FROM Message m2 WHERE " +
           "m2.expediteur.id = :utilisateurId OR m2.destinataire.id = :utilisateurId " +
           "GROUP BY CASE WHEN m2.expediteur.id = :utilisateurId THEN m2.destinataire.id ELSE m2.expediteur.id END" +
           ") ORDER BY m.dateEnvoi DESC")
   List<Message> findDerniersMessagesParContact(@Param("utilisateurId") Long utilisateurId);

   List<Message> findByExpediteurIdOrderByDateEnvoiDesc(Long expediteurId);
   List<Message> findByDestinataireIdOrderByDateEnvoiDesc(Long destinataireId);
   List<Message> findByDateEnvoiBetween(LocalDateTime dateDebut, LocalDateTime dateFin);
   List<Message> findByDateEnvoiAfterOrderByDateEnvoiDesc(LocalDateTime date);
}
