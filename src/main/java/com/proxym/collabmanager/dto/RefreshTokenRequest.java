package com.proxym.collabmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RefreshTokenRequest {
    @NotBlank(message = "Le token est obligatoire")
    @NotNull(message = "Le token ne peut pas être null")
    @Size(min = 10, message = "Le token semble trop court")
    private String token;
}