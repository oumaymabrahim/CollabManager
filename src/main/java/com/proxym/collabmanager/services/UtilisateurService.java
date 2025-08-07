package com.proxym.collabmanager.services;

import com.proxym.collabmanager.entities.Utilisateur;
import com.proxym.collabmanager.enums.Role;
import com.proxym.collabmanager.repositories.UtilisateurRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UtilisateurService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;
    // Suppression de l'injection du PasswordEncoder pour éviter la dépendance circulaire
    // Le PasswordEncoder sera injecté dans AuthService où il est réellement nécessaire

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé: " + username));

        return utilisateur;
    }

    // Méthode pour sauvegarder un utilisateur (sans encoder le mot de passe)
    // L'encodage se fera dans AuthService
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



}
