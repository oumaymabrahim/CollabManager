package com.proxym.collabmanager.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="notification")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le titre de la notification est obligatoire")
    @Column(nullable = false, length = 200)
    private String titre;

    @NotBlank(message = "Le contenu de la notification est obligatoire")
    @Column(nullable = false, length = 1000)
    private String contenu;

    @NotNull(message = "La date d'envoi est obligatoire")
    @Column(nullable = false)
    private LocalDateTime dateEnvoi;

    @NotBlank(message = "Le type de notification est obligatoire")
    @Column(nullable = false, length = 50)
    private String type;

    @Column(nullable = false)
    private Boolean lue = false;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

}