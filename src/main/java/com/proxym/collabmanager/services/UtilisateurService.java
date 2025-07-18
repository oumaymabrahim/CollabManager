package com.proxym.collabmanager.services;

import com.proxym.collabmanager.entities.Utilisateur;
import com.proxym.collabmanager.enums.Role;
import com.proxym.collabmanager.enums.StatutTache;
import com.proxym.collabmanager.repositories.UtilisateurRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UtilisateurService implements UserDetailsService {

    @Autowired
    private final UtilisateurRepository utilisateurRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé: " + username));

        return utilisateur;
    }

    // Créer un nouvel utilisateur avec mot de passe encodé
    public Utilisateur createUtilisateur(Utilisateur utilisateur) {
        // Encoder le mot de passe
        utilisateur.setMotDePasse(passwordEncoder.encode(utilisateur.getMotDePasse()));
        return utilisateurRepository.save(utilisateur);
    }

    // Créer ou mettre à jour un utilisateur
    public Utilisateur saveUtilisateur(Utilisateur utilisateur) {
        return utilisateurRepository.save(utilisateur);
    }

    // Supprimer un utilisateur
    public void deleteUtilisateur(Long id) {
        utilisateurRepository.deleteById(id);
    }

    // Récupérer un utilisateur par ID
    public Optional<Utilisateur> getUtilisateurById(Long id) {
        return utilisateurRepository.findById(id);
    }

    // Récupérer tous les utilisateurs
    public List<Utilisateur> getAllUtilisateurs() {
        return utilisateurRepository.findAll();
    }

    // Recherche par email
    public Optional<Utilisateur> getByEmail(String email) {
        return utilisateurRepository.findByEmail(email);
    }

    // Vérifier si un email existe
    public boolean emailExiste(String email) {
        return utilisateurRepository.existsByEmail(email);
    }

    // Recherche par nom (partiel, insensible à la casse)
    public List<Utilisateur> getUtilisateursParNom(String nom) {
        return utilisateurRepository.findByNomContainingIgnoreCase(nom);
    }

    // Recherche par rôle
    public List<Utilisateur> getUtilisateursParRole(Role role) {
        return utilisateurRepository.findByRole(role);
    }

    // Compter les utilisateurs par rôle
    public Long countByRole(Role role) {
        return utilisateurRepository.countByRole(role);
    }

    // Utilisateurs participants à un projet
    public List<Utilisateur> getUtilisateursParProjet(Long projetId) {
        return utilisateurRepository.findByProjetsId(projetId);
    }

    // Utilisateurs ayant des tâches dans un projet
    public List<Utilisateur> getUtilisateursAvecTachesDansProjet(Long projetId) {
        return utilisateurRepository.findDistinctByTaches_Projet_Id(projetId);
    }

    // Utilisateurs actifs (avec tâches en cours)
    public List<Utilisateur> getUtilisateursActifs() {
        return utilisateurRepository.findDistinctByTachesStatut(StatutTache.EN_COURS);
    }
}