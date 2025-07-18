package com.proxym.collabmanager.controllers;

import com.proxym.collabmanager.entities.Message;
import com.proxym.collabmanager.services.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;

    //  Obtenir la conversation entre deux utilisateurs
    @GetMapping("/conversation")
    public List<Message> getConversation(
            @RequestParam Long utilisateur1Id,
            @RequestParam Long utilisateur2Id) {
        return messageService.getConversation(utilisateur1Id, utilisateur2Id);
    }

    //  Compter les messages échangés entre deux utilisateurs
    @GetMapping("/conversation/count")
    public Long countConversation(
            @RequestParam Long utilisateur1Id,
            @RequestParam Long utilisateur2Id) {
        return messageService.countConversation(utilisateur1Id, utilisateur2Id);
    }

    // Derniers messages par contact
    @GetMapping("/dernier-par-contact")
    public List<Message> getDerniersMessagesParContact(@RequestParam Long utilisateurId) {
        return messageService.getDerniersMessagesParContact(utilisateurId);
    }

    //  Messages envoyés par un utilisateur
    @GetMapping("/envoyes")
    public List<Message> getMessagesEnvoyes(@RequestParam Long expediteurId) {
        return messageService.getMessagesEnvoyes(expediteurId);
    }

    //  Messages reçus par un utilisateur
    @GetMapping("/recus")
    public List<Message> getMessagesRecus(@RequestParam Long destinataireId) {
        return messageService.getMessagesRecus(destinataireId);
    }

    // Messages entre deux dates
    @GetMapping("/entre-dates")
    public List<Message> getMessagesEntreDates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFin) {
        return messageService.getMessagesEntreDates(dateDebut, dateFin);
    }

    //  Messages récents après une certaine date
    @GetMapping("/recents")
    public List<Message> getMessagesRecents(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        return messageService.getMessagesRecents(date);
    }

    // Créer un nouveau message
    @PostMapping
    public Message createMessage(@RequestBody Message message) {
        return messageService.saveMessage(message);
    }

    //  Supprimer un message par ID
    @DeleteMapping("/{id}")
    public void deleteMessage(@PathVariable Long id) {
        messageService.deleteMessage(id);
    }




}
