//package com.proxym.collabmanager.controllers;
//
//import com.proxym.collabmanager.dto.AuthResponse;
//import com.proxym.collabmanager.dto.LoginRequest;
//import com.proxym.collabmanager.dto.RegisterRequest;
//import com.proxym.collabmanager.entities.Utilisateur;
//import com.proxym.collabmanager.security.JwtUtils;
//import com.proxym.collabmanager.services.UtilisateurService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/auth")
//@RequiredArgsConstructor
//@CrossOrigin(origins = "*", maxAge = 3600)
//public class AuthController {
//
//    @Autowired
//    private AuthenticationManager authenticationManager;
//
//    @Autowired
//    private UtilisateurService utilisateurService;
//
//    @Autowired
//    private JwtUtils jwtUtils;
//
//    @PostMapping("/login")
//    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
//        try {
//            Authentication authentication = authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getMotDePasse()));
//
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//            String jwt = jwtUtils.generateJwtToken(authentication);
//
//            Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
//
//            return ResponseEntity.ok(new AuthResponse(jwt,
//                    utilisateur.getId(),
//                    utilisateur.getNom(),
//                    utilisateur.getEmail(),
//                    utilisateur.getRole()));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest()
//                    .body("Erreur: Email ou mot de passe incorrect!");
//        }
//    }
//
//    @PostMapping("/register")
//    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
//        if (utilisateurService.emailExiste(registerRequest.getEmail())) {
//            return ResponseEntity.badRequest()
//                    .body("Erreur: Email déjà utilisé!");
//        }
//
//        // Créer le nouvel utilisateur
//        Utilisateur utilisateur = new Utilisateur();
//        utilisateur.setNom(registerRequest.getNom());
//        utilisateur.setEmail(registerRequest.getEmail());
//        utilisateur.setMotDePasse(registerRequest.getMotDePasse());
//        utilisateur.setRole(registerRequest.getRole());
//
//        utilisateur = utilisateurService.createUtilisateur(utilisateur);
//
//        return ResponseEntity.ok().body("Utilisateur enregistré avec succès!");
//    }
//
//    @GetMapping("/me")
//    public ResponseEntity<?> getCurrentUser() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        if (authentication != null && authentication.isAuthenticated()) {
//            Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
//            return ResponseEntity.ok(new AuthResponse(null,
//                    utilisateur.getId(),
//                    utilisateur.getNom(),
//                    utilisateur.getEmail(),
//                    utilisateur.getRole()));
//        }
//
//        return ResponseEntity.badRequest().body("Utilisateur non authentifié");
//    }
//}