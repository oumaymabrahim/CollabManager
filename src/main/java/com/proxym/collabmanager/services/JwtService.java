package com.proxym.collabmanager.services;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    // Clé secrète pour signer les tokens (à mettre dans application.properties)
    @Value("${jwt.secret}")
    private String secretKey;

    // Durée d'expiration du token (24 heures par défaut)
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Génère la clé de signature
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    /**
     * Extrait le nom d'utilisateur du token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrait la date d'expiration du token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrait un claim spécifique du token
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrait tous les claims du token
     */

// 2. CORRECTION DANS JwtService.java - Nouvelle méthode pour gérer les tokens expirés

    public String extractUsernameFromExpiredToken(String token) {
        try {
            // Validation initiale
            if (token == null || token.trim().isEmpty()) {
                throw new RuntimeException("Token est null ou vide");
            }

            // Essayer d'extraire normalement d'abord
            return extractUsername(token);

        } catch (Exception e) {
            // Si échec, essayer d'extraire même si expiré
            try {
                Claims claims = Jwts.parser()
                        .verifyWith(getSigningKey())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
                return claims.getSubject();

            } catch (ExpiredJwtException ex) {
                // Token expiré, mais on peut extraire les claims
                return ex.getClaims().getSubject();

            } catch (Exception ex) {
                throw new RuntimeException("Impossible d'extraire le username du token: " + ex.getMessage());
            }
        }
    }

// 3. CORRECTION DANS JwtService.java - Méthode extractAllClaims améliorée

    private Claims extractAllClaims(String token) {
        try {
            // Validation du token
            if (token == null || token.trim().isEmpty()) {
                throw new RuntimeException("Token JWT est null ou vide");
            }

            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

        } catch (ExpiredJwtException e) {
            // Pour le refresh token, on accepte les tokens expirés
            return e.getClaims();

        } catch (JwtException e) {
            throw new RuntimeException("Token JWT invalide: " + e.getMessage(), e);

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'analyse du token: " + e.getMessage(), e);
        }
    }




    /**
     * Vérifie si le token est expiré
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Génère un token pour un utilisateur
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Génère un token avec des claims supplémentaires
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    /**
     * Construit le token JWT
     */
    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        var authorities = userDetails.getAuthorities()
                .stream()
                .map(authority -> authority.getAuthority())
                .toList();

        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .claim("authorities", authorities)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusMillis(expiration)))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Valide le token
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Rafraîchit un token
     */
    public String refreshToken(UserDetails userDetails) {
        return generateToken(userDetails);
    }
}