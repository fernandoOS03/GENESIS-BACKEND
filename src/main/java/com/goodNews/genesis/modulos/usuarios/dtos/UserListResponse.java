package com.goodNews.genesis.modulos.usuarios.dtos;

import com.goodNews.genesis.shared.enums.UsersEnum;

import java.util.UUID;

public record UserListResponse(
        UUID id,
        String nombre,
        String email,
        String password,
        UsersEnum rol,
        String pais,
        Integer estado
) {
}
