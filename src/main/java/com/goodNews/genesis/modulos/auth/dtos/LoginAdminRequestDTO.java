package com.goodNews.genesis.modulos.auth.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginAdminRequestDTO(
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Email inválido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        String password
) {
}
