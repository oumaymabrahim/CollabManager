package com.proxym.collabmanager.dto;


import com.proxym.collabmanager.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private Long userId;
    private String nom;
    private String email;
    private Role role;
    private String message;
}
