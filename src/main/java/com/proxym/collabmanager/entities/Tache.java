package com.proxym.collabmanager.entities;

import com.proxym.collabmanager.enums.Priorite;
import com.proxym.collabmanager.enums.StatutProjet;
import com.proxym.collabmanager.enums.StatutTache;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name= "tache")
public class Tache {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private long id;


    @NotBlank(message = "Le nom de la tâche est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    @Column(nullable = false, length = 100)
    private String nom;

    @Column(length = 1000)
    private String description;

    @NotNull(message = "La date de création est obligatoire")
    @Column(nullable = false)
    private LocalDate dateCreation;

    @NotNull(message = "La date d'échéance est obligatoire")
    @Column(nullable = false)
    private LocalDate dateEcheance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutTache statut;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priorite priorite;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projet_id", nullable = false)
    private Projet projet;

}
