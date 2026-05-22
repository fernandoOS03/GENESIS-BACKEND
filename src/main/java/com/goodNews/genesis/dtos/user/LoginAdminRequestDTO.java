package com.goodNews.genesis.dtos.user;

import com.goodNews.genesis.enums.UsersEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginAdminRequestDTO(

        @NotBlank(message = "El email es obligitario")
        @Email(message = "Email inválido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        String password

) {

}
