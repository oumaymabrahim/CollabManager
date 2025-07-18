package com.proxym.collabmanager.services;

import com.proxym.collabmanager.entities.Notification;
import com.proxym.collabmanager.repositories.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {


        private final NotificationRepository notificationRepository;

        //  Obtenir toutes les notifications d’un utilisateur
        public List<Notification> getNotificationsByUtilisateur(Long utilisateurId) {
            return notificationRepository.findByUtilisateurIdOrderByDateEnvoiDesc(utilisateurId);
        }

        //  Obtenir les notifications non lues
        public List<Notification> getNotificationsNonLues(Long utilisateurId) {
            return notificationRepository.findByUtilisateurIdAndLueFalseOrderByDateEnvoiDesc(utilisateurId);
        }

        //  Obtenir les notifications lues
        public List<Notification> getNotificationsLues(Long utilisateurId) {
            return notificationRepository.findByUtilisateurIdAndLueTrueOrderByDateEnvoiDesc(utilisateurId);
        }

        // Obtenir les notifications par type
        public List<Notification> getNotificationsParType(String type, Long utilisateurId) {
            return notificationRepository.findByTypeAndUtilisateurIdOrderByDateEnvoiDesc(type, utilisateurId);
        }

        //  Obtenir les notifications envoyées dans une période
        public List<Notification> getNotificationsDansPeriode(LocalDateTime debut, LocalDateTime fin) {
            return notificationRepository.findByDateEnvoiBetween(debut, fin);
        }

        //  Compter les notifications non lues
        public Long countNotificationsNonLues(Long utilisateurId) {
            return notificationRepository.countNotificationsNonLues(utilisateurId);
        }

        //  Marquer toutes les notifications comme lues
        @Transactional
        public int marquerToutesCommeLues(Long utilisateurId) {
            return notificationRepository.marquerToutesCommeLues(utilisateurId);
        }

        //  Supprimer les anciennes notifications (ex. : plus de 30 jours)
        @Transactional
        public int supprimerAnciennesNotifications(LocalDateTime dateLimite) {
            return notificationRepository.supprimerAnciennesNotifications(dateLimite);
        }

        //  Récupérer les notifications des dernières 24 heures
        public List<Notification> getNotificationsRecentes(LocalDateTime date24hAgo) {
            return notificationRepository.findNotificationsRecentes(date24hAgo);
        }

        //  Ajouter une notification
        public Notification saveNotification(Notification notification) {
            return notificationRepository.save(notification);
        }

        //  Supprimer une notification par ID
        public void deleteNotification(Long id) {
            notificationRepository.deleteById(id);
        }

        // Récupérer une notification par ID
        public Notification getNotification(Long id) {
            return notificationRepository.findById(id).orElse(null);
        }
    }



