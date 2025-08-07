package com.proxym.collabmanager.config;

import com.proxym.collabmanager.services.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Lazy // Utilisation de @Lazy pour résoudre la dépendance circulaire
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UtilisateurService utilisateurService;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Endpoints publics
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/refresh",
                                "/api/auth/validate",
                                "/api/auth/check-email",
                                "/api/auth/logout",
                                "/api/auth/profil",
                                "/api/utilisateurs/inscription"
                        ).permitAll()

                        // PROJETS - Utiliser hasAuthority avec ROLE_ prefix
                        .requestMatchers(HttpMethod.POST, "/api/projets/add").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/projets/all", "/api/projets/search").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/projets/{id}/delete").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/projets/{projetId}/assigner/{utilisateurId}").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/projets/{projetId}/retirer/{utilisateurId}").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/projets/sans-participants").hasAuthority("ROLE_ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/projets/{id}/statistiques").hasAnyAuthority("ROLE_ADMIN", "ROLE_CHEF_DE_PROJECT")
                        .requestMatchers(HttpMethod.PUT, "/api/projets/{id}/statut").hasAnyAuthority("ROLE_ADMIN", "ROLE_CHEF_DE_PROJECT")

                        .requestMatchers(HttpMethod.GET, "/api/projets/statut").hasAnyAuthority("ROLE_ADMIN", "ROLE_CHEF_DE_PROJECT", "ROLE_MEMBRE_EQUIPE")
                        .requestMatchers(HttpMethod.GET, "/api/projets/{id}/projet").hasAnyAuthority("ROLE_ADMIN", "ROLE_CHEF_DE_PROJECT", "ROLE_MEMBRE_EQUIPE")
                        .requestMatchers(HttpMethod.GET, "/api/projets/{id}/participants").hasAnyAuthority("ROLE_ADMIN", "ROLE_CHEF_DE_PROJECT", "ROLE_MEMBRE_EQUIPE")
                        .requestMatchers(HttpMethod.GET, "/api/projets/mes-projets").hasAnyAuthority("ROLE_CHEF_DE_PROJECT", "ROLE_MEMBRE_EQUIPE")

                        // TACHES
                        .requestMatchers(HttpMethod.POST, "/api/taches/add").hasAuthority("ROLE_CHEF_DE_PROJECT")
                        .requestMatchers(HttpMethod.PUT, "/api/taches/{id}/update").hasAuthority("ROLE_CHEF_DE_PROJECT")
                        .requestMatchers(HttpMethod.DELETE, "/api/taches/{id}/delete").hasAuthority("ROLE_CHEF_DE_PROJECT")
                        .requestMatchers(HttpMethod.GET, "/api/taches/all").hasAuthority("ROLE_CHEF_DE_PROJECT")
                        .requestMatchers(HttpMethod.GET, "/api/taches/{id}/tache").hasAuthority("ROLE_CHEF_DE_PROJECT")
                        .requestMatchers(HttpMethod.GET, "/api/taches/utilisateur/{id}").hasAuthority("ROLE_CHEF_DE_PROJECT")
                        .requestMatchers(HttpMethod.GET, "/api/taches/projet/{id}").hasAuthority("ROLE_CHEF_DE_PROJECT")
                        .requestMatchers(HttpMethod.GET, "/api/taches/utilisateur/{id}/statut").hasAuthority("ROLE_CHEF_DE_PROJECT")

                        .requestMatchers(HttpMethod.GET, "/api/taches/mes-taches").hasAuthority("ROLE_MEMBRE_EQUIPE")
                        .requestMatchers(HttpMethod.PUT, "/api/taches/{id}/update-statut").hasAuthority("ROLE_MEMBRE_EQUIPE")

                        .requestMatchers(HttpMethod.GET, "/api/taches/statut").hasAnyAuthority("ROLE_CHEF_DE_PROJECT", "ROLE_MEMBRE_EQUIPE")

                        // UTILISATEURS - ADMIN (CORRIGER : Utiliser ROLE_ADMIN partout)
                        .requestMatchers("/api/utilisateurs/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/utilisateurs/all").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/utilisateurs/{id}").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/utilisateurs/email").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/utilisateurs/nom").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/utilisateurs/role").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/utilisateurs/count").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/utilisateurs/{id}").hasAuthority("ROLE_ADMIN")

                        // PROFIL PERSONNEL
                        .requestMatchers("/api/utilisateurs/mon-profil").hasAnyAuthority("ROLE_ADMIN", "ROLE_CHEF_DE_PROJECT", "ROLE_MEMBRE_EQUIPE")
                        .requestMatchers("/api/utilisateurs/mon-profil/mot-de-passe").hasAnyAuthority("ROLE_ADMIN", "ROLE_CHEF_DE_PROJECT", "ROLE_MEMBRE_EQUIPE")
                        .requestMatchers("/api/auth/profile").hasAnyAuthority("ROLE_ADMIN", "ROLE_CHEF_DE_PROJECT", "ROLE_MEMBRE_EQUIPE")

                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(utilisateurService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Plus sécurisé : spécifier les origines autorisées
        config.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
