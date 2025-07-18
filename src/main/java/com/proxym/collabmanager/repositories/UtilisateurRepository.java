package com.proxym.collabmanager.repositories;

import com.proxym.collabmanager.entities.Utilisateur;
import com.proxym.collabmanager.enums.Role;
import com.proxym.collabmanager.enums.StatutTache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
       // Recherche par email (pour l'authentification)
       Optional<Utilisateur> findByEmail(String email);

       // Vérifier si un email existe déjà
       boolean existsByEmail(String email);

       // Recherche par nom (insensible à la casse)
       List<Utilisateur> findByNomContainingIgnoreCase(String nom);

       // Recherche par rôle
       List<Utilisateur> findByRole(Role role);

       // Compter les utilisateurs par rôle
       Long countByRole(Role role);

       // Recherche des utilisateurs participant à un projet (relation ManyToMany)
       List<Utilisateur> findByProjetsId(Long projetId);

       // Recherche des utilisateurs ayant des tâches dans un projet
       List<Utilisateur> findDistinctByTaches_Projet_Id(Long projetId);

       // Recherche des utilisateurs actifs (ayant des tâches EN_COURS)
       List<Utilisateur> findDistinctByTachesStatut(StatutTache statut);
            }
