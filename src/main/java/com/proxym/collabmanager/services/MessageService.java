package com.proxym.collabmanager.services;

import com.proxym.collabmanager.entities.Message;
import com.proxym.collabmanager.repositories.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;

    // Récupérer la conversation entre deux utilisateurs
    public List<Message> getConversation(Long utilisateur1Id, Long utilisateur2Id) {
        return messageRepository.findConversation(utilisateur1Id, utilisateur2Id);
    }

    // Compter les messages échangés entre deux utilisateurs
    public Long countConversation(Long utilisateur1Id, Long utilisateur2Id) {
        return messageRepository.countConversation(utilisateur1Id, utilisateur2Id);
    }

    // Récupérer les derniers messages par contact pour un utilisateur
    public List<Message> getDerniersMessagesParContact(Long utilisateurId) {
        return messageRepository.findDerniersMessagesParContact(utilisateurId);
    }

    // Récupérer les messages envoyés par un utilisateur, triés par date décroissante
    public List<Message> getMessagesEnvoyes(Long expediteurId) {
        return messageRepository.findByExpediteurIdOrderByDateEnvoiDesc(expediteurId);
    }

    // Récupérer les messages reçus par un utilisateur, triés par date décroissante
    public List<Message> getMessagesRecus(Long destinataireId) {
        return messageRepository.findByDestinataireIdOrderByDateEnvoiDesc(destinataireId);
    }

    // Récupérer les messages envoyés entre deux dates
    public List<Message> getMessagesEntreDates(LocalDateTime dateDebut, LocalDateTime dateFin) {
        return messageRepository.findByDateEnvoiBetween(dateDebut, dateFin);
    }

    // Récupérer les messages envoyés après une certaine date, triés par date décroissante
    public List<Message> getMessagesRecents(LocalDateTime date) {
        return messageRepository.findByDateEnvoiAfterOrderByDateEnvoiDesc(date);
    }

    // Sauvegarder un message
    public Message saveMessage(Message message) {
        return messageRepository.save(message);
    }

    // Supprimer un message par son id
    public void deleteMessage(Long messageId) {
        messageRepository.deleteById(messageId);
    }

}
