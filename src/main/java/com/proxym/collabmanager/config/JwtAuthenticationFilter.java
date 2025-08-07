package com.proxym.collabmanager.config;

import com.proxym.collabmanager.services.JwtService;
import com.proxym.collabmanager.services.UtilisateurService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    @Lazy
    private final UtilisateurService utilisateurService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        System.out.println("=== JWT FILTER DEBUG START ===");
        System.out.println("Request URI: " + requestURI);
        System.out.println("Request Method: " + request.getMethod());

        // Récupérer l'en-tête Authorization
        final String authHeader = request.getHeader("Authorization");
        System.out.println("Auth Header: " + authHeader);

        // Vérifier si l'en-tête contient un token Bearer
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("No Bearer token found, continuing filter chain");
            filterChain.doFilter(request, response);
            return;
        }

        // Extraire le token (enlever "Bearer ")
        final String jwt = authHeader.substring(7);
        System.out.println("Extracted JWT: " + jwt.substring(0, Math.min(jwt.length(), 20)) + "...");

        try {
            // Extraire l'email du token
            final String userEmail = jwtService.extractUsername(jwt);
            System.out.println("Extracted email from JWT: " + userEmail);

            // Si l'email est trouvé et qu'aucune authentification n'existe déjà
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                System.out.println("Email found and no existing authentication, loading user details");

                // Charger les détails de l'utilisateur
                UserDetails userDetails = utilisateurService.loadUserByUsername(userEmail);
                System.out.println("Loaded user: " + userDetails.getUsername());
                System.out.println("User authorities: " + userDetails.getAuthorities());

                // Vérifier si le token est valide
                boolean tokenValid = jwtService.isTokenValid(jwt, userDetails);
                System.out.println("Token valid: " + tokenValid);

                if (tokenValid) {
                    // Créer le token d'authentification
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    // Ajouter les détails de la requête
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Définir l'authentification dans le contexte de sécurité
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("Authentication set in SecurityContext");
                    System.out.println("Final authorities: " + authToken.getAuthorities());
                } else {
                    System.out.println("Token validation failed");
                }
            } else {
                if (userEmail == null) {
                    System.out.println("No email extracted from token");
                }
                if (SecurityContextHolder.getContext().getAuthentication() != null) {
                    System.out.println("Authentication already exists");
                }
            }
        } catch (Exception e) {
            System.out.println("Exception in JWT processing: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("=== JWT FILTER DEBUG END ===");

        // Continuer avec le filtre suivant
        filterChain.doFilter(request, response);
    }
}
