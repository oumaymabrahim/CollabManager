package com.proxym.collabmanager.controllers;

import com.proxym.collabmanager.entities.Notification;
import com.proxym.collabmanager.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

        private final NotificationService notificationService;

        //  Obtenir toutes les notifications d’un utilisateur
        @GetMapping("/utilisateur/{id}")
        public List<Notification> getNotificationsByUtilisateur(@PathVariable Long id) {
            return notificationService.getNotificationsByUtilisateur(id);
        }

        //  Obtenir les notifications non lues
        @GetMapping("/non-lues/{utilisateurId}")
        public List<Notification> getNotificationsNonLues(@PathVariable Long utilisateurId) {
            return notificationService.getNotificationsNonLues(utilisateurId);
        }

        //  Obtenir les notifications lues
        @GetMapping("/lues/{utilisateurId}")
        public List<Notification> getNotificationsLues(@PathVariable Long utilisateurId) {
            return notificationService.getNotificationsLues(utilisateurId);
        }

        //  Obtenir les notifications d’un type spécifique pour un utilisateur
        @GetMapping("/type")
        public List<Notification> getNotificationsParType(
                @RequestParam String type,
                @RequestParam Long utilisateurId) {
            return notificationService.getNotificationsParType(type, utilisateurId);
        }

        //  Obtenir les notifications entre deux dates
        @GetMapping("/entre-dates")
        public List<Notification> getNotificationsEntreDates(
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime debut,
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
            return notificationService.getNotificationsDansPeriode(debut, fin);
        }

        //  Compter les notifications non lues d’un utilisateur
        @GetMapping("/non-lues/count/{utilisateurId}")
        public Long countNotificationsNonLues(@PathVariable Long utilisateurId) {
            return notificationService.countNotificationsNonLues(utilisateurId);
        }

        //  Marquer toutes les notifications comme lues
        @PutMapping("/marquer-toutes-lues/{utilisateurId}")
        public int marquerToutesCommeLues(@PathVariable Long utilisateurId) {
            return notificationService.marquerToutesCommeLues(utilisateurId);
        }

        //  Supprimer les anciennes notifications
        @DeleteMapping("/supprimer-anciennes")
        public int supprimerAnciennesNotifications(
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateLimite) {
            return notificationService.supprimerAnciennesNotifications(dateLimite);
        }

        // Notifications des dernières 24 heures
        @GetMapping("/recents")
        public List<Notification> getNotificationsRecentes(
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime depuis) {
            return notificationService.getNotificationsRecentes(depuis);
        }

        //  Créer une notification
        @PostMapping
        public Notification createNotification(@RequestBody Notification notification) {
            return notificationService.saveNotification(notification);
        }

        //  Supprimer une notification par ID
        @DeleteMapping("/{id}")
        public void deleteNotification(@PathVariable Long id) {
            notificationService.deleteNotification(id);
        }

        // Obtenir une notification par ID
        @GetMapping("/{id}")
        public Notification getNotification(@PathVariable Long id) {
            return notificationService.getNotification(id);
        }
    }


