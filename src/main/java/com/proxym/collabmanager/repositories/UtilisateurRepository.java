package com.proxym.collabmanager.repositories;


import com.proxym.collabmanager.entities.Utilisateur;
import com.proxym.collabmanager.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
       // Recherche par email (pour l'authentification)
       Optional<Utilisateur> findByEmail(String email);


       // Recherche par nom (insensible à la casse)
       List<Utilisateur> findByNomContainingIgnoreCase(String nom);

       // Recherche par rôle
       List<Utilisateur> findByRole(Role role);

       // Compter les utilisateurs par rôle
       Long countByRole(Role role);


       boolean existsByEmail(@Email(message = "Format d'email invalide")
                             @NotBlank(message = "L'email est obligatoire") String email);



}
